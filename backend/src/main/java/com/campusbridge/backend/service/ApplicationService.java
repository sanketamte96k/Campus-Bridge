package com.campusbridge.backend.service;

import com.campusbridge.backend.dto.*;
import com.campusbridge.backend.entity.Application;
import com.campusbridge.backend.entity.Job;
import com.campusbridge.backend.entity.User;
import com.campusbridge.backend.enums.ApplicationStatus;
import com.campusbridge.backend.exception.BadRequestException;
import com.campusbridge.backend.exception.ForbiddenException;
import com.campusbridge.backend.exception.ResourceNotFoundException;
import com.campusbridge.backend.repository.ApplicationRepository;
import com.campusbridge.backend.repository.JobRepository;
import com.campusbridge.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final StudentProfileService profileService;
    private final EligibilityService eligibilityService;
    private final JobService jobService;

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> ALLOWED_TRANSITIONS = Map.of(
            ApplicationStatus.APPLIED, Set.of(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED),
            ApplicationStatus.SHORTLISTED, Set.of(ApplicationStatus.SELECTED, ApplicationStatus.REJECTED),
            ApplicationStatus.SELECTED, Collections.emptySet(),
            ApplicationStatus.REJECTED, Collections.emptySet(),
            ApplicationStatus.WITHDRAWN, Collections.emptySet()
    );

    public ApplicationService(
            ApplicationRepository applicationRepository,
            JobRepository jobRepository,
            UserRepository userRepository,
            StudentProfileService profileService,
            EligibilityService eligibilityService,
            JobService jobService
    ) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.profileService = profileService;
        this.eligibilityService = eligibilityService;
        this.jobService = jobService;
    }

    public boolean canTransition(ApplicationStatus from, ApplicationStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return ALLOWED_TRANSITIONS.getOrDefault(from, Collections.emptySet()).contains(to);
    }

    @Transactional
    public ApplicationDto applyToJob(String jobId, String studentId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        // 1. Profile must exist
        StudentProfileDto profile = profileService.getProfileByUserId(studentId);
        if (profile == null) {
            throw new BadRequestException("Complete your profile before applying.");
        }

        // 2. Deadline check: open through the end of the deadline date (23:59:59)
        LocalDateTime deadlineEnd = job.getDeadline().atTime(LocalTime.MAX);
        if (LocalDateTime.now().isAfter(deadlineEnd)) {
            throw new BadRequestException("Applications for this job are closed.");
        }

        // 3. Existing active application check (status not WITHDRAWN)
        List<Application> userApplications = applicationRepository.findByUserId(studentId);
        Optional<Application> existingActive = userApplications.stream()
                .filter(app -> app.getJobId().equals(jobId) && app.getStatus() != ApplicationStatus.WITHDRAWN)
                .findFirst();

        if (existingActive.isPresent()) {
            throw new BadRequestException("You've already applied to this job.");
        }

        // 4. Eligibility check
        JobDto jobDto = jobService.toDto(job);
        EligibilityResultDto eligibility = eligibilityService.evaluateEligibility(profile, jobDto.getRules());
        if (!eligibility.isEligible()) {
            throw new BadRequestException("You're not eligible for this job.");
        }

        // Check if there was a previously withdrawn application
        Optional<Application> previouslyWithdrawn = userApplications.stream()
                .filter(app -> app.getJobId().equals(jobId) && app.getStatus() == ApplicationStatus.WITHDRAWN)
                .findFirst();

        Application application;
        if (previouslyWithdrawn.isPresent()) {
            application = previouslyWithdrawn.get();
            application.setStatus(ApplicationStatus.APPLIED);
            application.setAppliedAt(LocalDateTime.now());
        } else {
            application = new Application(jobId, studentId, ApplicationStatus.APPLIED);
            application.setAppliedAt(LocalDateTime.now());
        }

        Application saved = applicationRepository.save(application);
        return toDto(saved);
    }

    @Transactional
    public ApplicationDto withdrawApplication(String applicationId, String studentId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        if (!application.getUserId().equals(studentId)) {
            throw new ForbiddenException("You do not have permission to withdraw this application.");
        }

        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new BadRequestException("Only applications with status 'APPLIED' can be withdrawn.");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        Application saved = applicationRepository.save(application);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto> getMyApplications(String studentId) {
        return applicationRepository.findByUserIdOrderByAppliedAtDesc(studentId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicantDto> getApplicantsForJob(String jobId, String recruiterId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (job.getPostedBy() != null && !job.getPostedBy().equals(recruiterId)) {
            throw new ForbiddenException("You do not have permission to view applicants for this job.");
        }

        List<Application> applications = applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId);

        return applications.stream().map(app -> {
            User student = userRepository.findById(app.getUserId()).orElse(null);
            StudentProfileDto profileDto = profileService.getProfileByUserId(app.getUserId());

            String name = student != null ? student.getName() : "Unknown Student";
            String email = student != null ? student.getEmail() : "";

            return new ApplicantDto(toDto(app), name, email, profileDto);
        }).collect(Collectors.toList());
    }

    @Transactional
    public ApplicationDto updateApplicationStatus(String applicationId, ApplicationStatus newStatus, String recruiterId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated job not found with id: " + application.getJobId()));

        if (job.getPostedBy() != null && !job.getPostedBy().equals(recruiterId)) {
            throw new ForbiddenException("You do not have permission to update status for this job application.");
        }

        if (!canTransition(application.getStatus(), newStatus)) {
            throw new BadRequestException(
                    "Invalid status transition from " + application.getStatus() + " to " + newStatus + "."
            );
        }

        application.setStatus(newStatus);
        Application saved = applicationRepository.save(application);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public RecruiterStatsDto getRecruiterStats(String recruiterId) {
        List<Job> jobs = jobRepository.findByPostedBy(recruiterId);
        long totalJobs = jobs.size();

        if (totalJobs == 0) {
            return new RecruiterStatsDto(0, 0, 0, 0, 0, 0);
        }

        List<String> jobIds = jobs.stream().map(Job::getId).collect(Collectors.toList());
        List<Application> applications = applicationRepository.findByJobIdIn(jobIds);

        long totalApplications = applications.size();
        long shortlisted = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED).count();
        long selected = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.SELECTED).count();
        long rejected = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();
        long pendingReview = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.APPLIED).count();

        return new RecruiterStatsDto(totalJobs, totalApplications, shortlisted, selected, rejected, pendingReview);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getApplicantCounts(String recruiterId) {
        List<Job> jobs = jobRepository.findByPostedBy(recruiterId);
        if (jobs.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> jobIds = jobs.stream().map(Job::getId).collect(Collectors.toList());
        List<Application> applications = applicationRepository.findByJobIdIn(jobIds);

        Map<String, Long> counts = new HashMap<>();
        for (String jobId : jobIds) {
            counts.put(jobId, 0L);
        }

        for (Application app : applications) {
            if (app.getStatus() != ApplicationStatus.WITHDRAWN) {
                counts.put(app.getJobId(), counts.getOrDefault(app.getJobId(), 0L) + 1L);
            }
        }

        return counts;
    }

    public ApplicationDto toDto(Application app) {
        if (app == null) {
            return null;
        }
        return new ApplicationDto(
                app.getId(),
                app.getJobId(),
                app.getUserId(),
                app.getStatus(),
                app.getAppliedAt() != null ? app.getAppliedAt().format(DateTimeFormatter.ISO_DATE_TIME) : null
        );
    }
}
