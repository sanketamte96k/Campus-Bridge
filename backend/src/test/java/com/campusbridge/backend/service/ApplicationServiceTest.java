package com.campusbridge.backend.service;

import com.campusbridge.backend.dto.*;
import com.campusbridge.backend.entity.Application;
import com.campusbridge.backend.entity.EligibilityRuleEmbeddable;
import com.campusbridge.backend.entity.Job;
import com.campusbridge.backend.enums.ApplicationStatus;
import com.campusbridge.backend.enums.Branch;
import com.campusbridge.backend.exception.BadRequestException;
import com.campusbridge.backend.exception.ForbiddenException;
import com.campusbridge.backend.repository.ApplicationRepository;
import com.campusbridge.backend.repository.JobRepository;
import com.campusbridge.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Application Service Tests")
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileService profileService;

    @Mock
    private EligibilityService eligibilityService;

    @Mock
    private JobService jobService;

    @InjectMocks
    private ApplicationService applicationService;

    private Job openJob;
    private Job expiredJob;
    private StudentProfileDto eligibleProfile;
    private JobDto jobDto;

    @BeforeEach
    void setUp() {
        openJob = new Job(
                "Java developer",
                "Northwind Technologies",
                "Pune",
                6.0,
                LocalDate.now().plusDays(5),
                "Sample description",
                new EligibilityRuleEmbeddable(List.of(Branch.CS), new BigDecimal("7.0"), 0, List.of("Java")),
                "recruiter-1"
        );
        openJob.setId("job-1");

        expiredJob = new Job(
                "Expired Job",
                "Past Corp",
                "Pune",
                6.0,
                LocalDate.now().minusDays(1),
                "Past description",
                new EligibilityRuleEmbeddable(List.of(Branch.CS), new BigDecimal("7.0"), 0, List.of("Java")),
                "recruiter-1"
        );
        expiredJob.setId("job-expired");

        eligibleProfile = new StudentProfileDto(Branch.CS, new BigDecimal("8.0"), 0, List.of("Java"));
        jobDto = new JobDto("job-1", "Java developer", "Northwind Technologies", "Pune", 6.0,
                LocalDate.now().plusDays(5).toString(), "Sample description",
                new EligibilityRuleDto(List.of(Branch.CS), new BigDecimal("7.0"), 0, List.of("Java")), "recruiter-1");
    }

    @Test
    @DisplayName("Should throw BadRequestException if student has not completed profile")
    void shouldThrowIfProfileMissing() {
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(openJob));
        when(profileService.getProfileByUserId("student-1")).thenReturn(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> applicationService.applyToJob("job-1", "student-1"));
        assertEquals("Complete your profile before applying.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException if job deadline has passed")
    void shouldThrowIfDeadlinePassed() {
        when(jobRepository.findById("job-expired")).thenReturn(Optional.of(expiredJob));
        when(profileService.getProfileByUserId("student-1")).thenReturn(eligibleProfile);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> applicationService.applyToJob("job-expired", "student-1"));
        assertEquals("Applications for this job are closed.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException if active application already exists")
    void shouldThrowIfAlreadyApplied() {
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(openJob));
        when(profileService.getProfileByUserId("student-1")).thenReturn(eligibleProfile);

        Application existing = new Application("job-1", "student-1", ApplicationStatus.APPLIED);
        when(applicationRepository.findByUserId("student-1")).thenReturn(List.of(existing));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> applicationService.applyToJob("job-1", "student-1"));
        assertEquals("You've already applied to this job.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException if student is not eligible")
    void shouldThrowIfNotEligible() {
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(openJob));
        when(profileService.getProfileByUserId("student-1")).thenReturn(eligibleProfile);
        when(applicationRepository.findByUserId("student-1")).thenReturn(Collections.emptyList());
        when(jobService.toDto(openJob)).thenReturn(jobDto);

        EligibilityResultDto ineligibility = new EligibilityResultDto(false, List.of());
        when(eligibilityService.evaluateEligibility(eligibleProfile, jobDto.getRules())).thenReturn(ineligibility);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> applicationService.applyToJob("job-1", "student-1"));
        assertEquals("You're not eligible for this job.", ex.getMessage());
    }

    @Test
    @DisplayName("Should apply successfully when all requirements met")
    void shouldApplySuccessfully() {
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(openJob));
        when(profileService.getProfileByUserId("student-1")).thenReturn(eligibleProfile);
        when(applicationRepository.findByUserId("student-1")).thenReturn(Collections.emptyList());
        when(jobService.toDto(openJob)).thenReturn(jobDto);

        EligibilityResultDto eligibleResult = new EligibilityResultDto(true, List.of());
        when(eligibilityService.evaluateEligibility(eligibleProfile, jobDto.getRules())).thenReturn(eligibleResult);

        Application savedApp = new Application("job-1", "student-1", ApplicationStatus.APPLIED);
        savedApp.setId("app-1");
        savedApp.setAppliedAt(LocalDateTime.now());
        when(applicationRepository.save(any(Application.class))).thenReturn(savedApp);

        ApplicationDto result = applicationService.applyToJob("job-1", "student-1");

        assertNotNull(result);
        assertEquals("app-1", result.getId());
        assertEquals("job-1", result.getJobId());
        assertEquals("student-1", result.getUserId());
        assertEquals(ApplicationStatus.APPLIED, result.getStatus());
    }

    @Test
    @DisplayName("Should withdraw application when in APPLIED status")
    void shouldWithdrawWhenApplied() {
        Application application = new Application("job-1", "student-1", ApplicationStatus.APPLIED);
        application.setId("app-1");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationDto withdrawn = applicationService.withdrawApplication("app-1", "student-1");

        assertEquals(ApplicationStatus.WITHDRAWN, withdrawn.getStatus());
    }

    @Test
    @DisplayName("Should throw BadRequestException if withdrawing non-APPLIED application")
    void shouldThrowIfWithdrawingNonApplied() {
        Application application = new Application("job-1", "student-1", ApplicationStatus.SHORTLISTED);
        application.setId("app-1");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(application));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> applicationService.withdrawApplication("app-1", "student-1"));
        assertEquals("Only applications with status 'APPLIED' can be withdrawn.", ex.getMessage());
    }

    @Test
    @DisplayName("Should update application status when valid transition")
    void shouldUpdateStatusWhenValidTransition() {
        Application application = new Application("job-1", "student-1", ApplicationStatus.APPLIED);
        application.setId("app-1");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(application));
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(openJob));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationDto updated = applicationService.updateApplicationStatus("app-1", ApplicationStatus.SHORTLISTED, "recruiter-1");

        assertEquals(ApplicationStatus.SHORTLISTED, updated.getStatus());
    }

    @Test
    @DisplayName("Should throw BadRequestException on invalid status transition")
    void shouldThrowOnInvalidTransition() {
        Application application = new Application("job-1", "student-1", ApplicationStatus.APPLIED);
        application.setId("app-1");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(application));
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(openJob));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> applicationService.updateApplicationStatus("app-1", ApplicationStatus.SELECTED, "recruiter-1"));
        assertTrue(ex.getMessage().contains("Invalid status transition"));
    }
}
