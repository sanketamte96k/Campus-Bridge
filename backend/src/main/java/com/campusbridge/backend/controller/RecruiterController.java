package com.campusbridge.backend.controller;

import com.campusbridge.backend.dto.ApplicantDto;
import com.campusbridge.backend.dto.JobDto;
import com.campusbridge.backend.dto.RecruiterStatsDto;
import com.campusbridge.backend.security.UserPrincipal;
import com.campusbridge.backend.service.ApplicationService;
import com.campusbridge.backend.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterController {

    private final JobService jobService;
    private final ApplicationService applicationService;

    public RecruiterController(JobService jobService, ApplicationService applicationService) {
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobDto>> getMyJobs(@AuthenticationPrincipal UserPrincipal principal) {
        List<JobDto> jobs = jobService.getJobsByRecruiter(principal.getId());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/jobs/{jobId}/applicants")
    public ResponseEntity<List<ApplicantDto>> getJobApplicants(
            @PathVariable String jobId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<ApplicantDto> applicants = applicationService.getApplicantsForJob(jobId, principal.getId());
        return ResponseEntity.ok(applicants);
    }

    @GetMapping("/stats")
    public ResponseEntity<RecruiterStatsDto> getRecruiterStats(@AuthenticationPrincipal UserPrincipal principal) {
        RecruiterStatsDto stats = applicationService.getRecruiterStats(principal.getId());
        return ResponseEntity.ok(stats);
    }
}
