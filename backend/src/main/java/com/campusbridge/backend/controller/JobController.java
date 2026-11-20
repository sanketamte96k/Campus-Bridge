package com.campusbridge.backend.controller;

import com.campusbridge.backend.dto.*;
import com.campusbridge.backend.exception.BadRequestException;
import com.campusbridge.backend.security.UserPrincipal;
import com.campusbridge.backend.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final ApplicationService applicationService;
    private final StudentProfileService profileService;
    private final EligibilityService eligibilityService;
    private final SkillGapService skillGapService;
    private final LearningPlanService learningPlanService;

    public JobController(
            JobService jobService,
            ApplicationService applicationService,
            StudentProfileService profileService,
            EligibilityService eligibilityService,
            SkillGapService skillGapService,
            LearningPlanService learningPlanService
    ) {
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.profileService = profileService;
        this.eligibilityService = eligibilityService;
        this.skillGapService = skillGapService;
        this.learningPlanService = learningPlanService;
    }

    @GetMapping
    public ResponseEntity<List<JobDto>> getJobs(@RequestParam(required = false) String search) {
        List<JobDto> jobs = (search != null && !search.isBlank())
                ? jobService.searchJobs(search)
                : jobService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJobById(@PathVariable String id) {
        JobDto job = jobService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobDto> createJob(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody NewJobRequest request
    ) {
        JobDto created = jobService.createJob(request, principal.getId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobDto> updateJob(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody NewJobRequest request
    ) {
        JobDto updated = jobService.updateJob(id, request, principal.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Void> deleteJob(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        jobService.deleteJob(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/applicants")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<ApplicantDto>> getJobApplicants(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<ApplicantDto> applicants = applicationService.getApplicantsForJob(id, principal.getId());
        return ResponseEntity.ok(applicants);
    }

    @GetMapping("/{id}/eligibility")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EligibilityResultDto> checkEligibility(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        JobDto job = jobService.getJobById(id);
        StudentProfileDto profile = profileService.getProfileByUserId(principal.getId());
        EligibilityResultDto result = eligibilityService.evaluateEligibility(profile, job.getRules());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/skill-gap")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SkillGapDto> getSkillGap(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        JobDto job = jobService.getJobById(id);
        StudentProfileDto profile = profileService.getProfileByUserId(principal.getId());
        if (profile == null) {
            throw new BadRequestException("Complete your profile first.");
        }
        SkillGapDto gap = skillGapService.computeGap(profile, job.getRules());
        return ResponseEntity.ok(gap);
    }

    @PostMapping("/{id}/learning-plan")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LearningPlanDto> createLearningPlan(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        JobDto job = jobService.getJobById(id);
        StudentProfileDto profile = profileService.getProfileByUserId(principal.getId());
        if (profile == null) {
            throw new BadRequestException("Complete your profile first.");
        }
        SkillGapDto gap = skillGapService.computeGap(profile, job.getRules());
        LearningPlanDto plan = learningPlanService.generatePlan(
                principal.getId(),
                job.getId(),
                job.getTitle(),
                job.getRules() != null ? job.getRules().getRequiredSkills() : List.of(),
                gap.getMissingSkills(),
                profile.getSkills()
        );
        return ResponseEntity.ok(plan);
    }
}
