package com.campusbridge.backend.service;

import com.campusbridge.backend.dto.EligibilityRuleDto;
import com.campusbridge.backend.dto.JobDto;
import com.campusbridge.backend.dto.NewJobRequest;
import com.campusbridge.backend.entity.EligibilityRuleEmbeddable;
import com.campusbridge.backend.entity.Job;
import com.campusbridge.backend.enums.Branch;
import com.campusbridge.backend.exception.ForbiddenException;
import com.campusbridge.backend.exception.ResourceNotFoundException;
import com.campusbridge.backend.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Job Service Tests")
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    private Job sampleJob;

    @BeforeEach
    void setUp() {
        sampleJob = new Job(
                "Java developer",
                "Northwind Technologies",
                "Pune",
                6.0,
                LocalDate.of(2026, 12, 15),
                "Sample description",
                new EligibilityRuleEmbeddable(List.of(Branch.CS, Branch.IT), new BigDecimal("7.5"), 0, List.of("Java", "SQL")),
                "recruiter-1"
        );
        sampleJob.setId("job-1");
    }

    @Test
    @DisplayName("Should successfully create a new job by recruiter")
    void shouldCreateJob() {
        NewJobRequest request = new NewJobRequest(
                "Java developer",
                "Northwind Technologies",
                "Pune",
                6.0,
                "2026-12-15",
                "Sample description",
                new EligibilityRuleDto(List.of(Branch.CS, Branch.IT), new BigDecimal("7.5"), 0, List.of("Java", "SQL"))
        );

        when(jobRepository.save(any(Job.class))).thenReturn(sampleJob);

        JobDto created = jobService.createJob(request, "recruiter-1");

        assertNotNull(created);
        assertEquals("job-1", created.getId());
        assertEquals("Java developer", created.getTitle());
        assertEquals("recruiter-1", created.getPostedBy());
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    @DisplayName("Should return job by ID if found")
    void shouldGetJobById() {
        when(jobRepository.findById("job-1")).thenReturn(Optional.of(sampleJob));

        JobDto found = jobService.getJobById("job-1");

        assertNotNull(found);
        assertEquals("job-1", found.getId());
        assertEquals("Northwind Technologies", found.getCompany());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when job not found")
    void shouldThrowWhenJobNotFound() {
        when(jobRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobService.getJobById("non-existent"));
    }

    @Test
    @DisplayName("Should update job if owned by recruiter")
    void shouldUpdateJobWhenOwned() {
        NewJobRequest request = new NewJobRequest(
                "Senior Java developer",
                "Northwind Technologies",
                "Pune",
                9.0,
                "2026-12-25",
                "Updated description",
                new EligibilityRuleDto(List.of(Branch.CS), new BigDecimal("8.0"), 0, List.of("Java"))
        );

        when(jobRepository.findById("job-1")).thenReturn(Optional.of(sampleJob));
        when(jobRepository.save(any(Job.class))).thenReturn(sampleJob);

        JobDto updated = jobService.updateJob("job-1", request, "recruiter-1");

        assertNotNull(updated);
        verify(jobRepository).save(sampleJob);
    }

    @Test
    @DisplayName("Should throw ForbiddenException when updating a job owned by another recruiter")
    void shouldThrowForbiddenWhenUpdatingOthersJob() {
        NewJobRequest request = new NewJobRequest(
                "Hacked Job",
                "Company",
                "Location",
                10.0,
                "2026-12-30",
                "Desc",
                new EligibilityRuleDto(List.of(Branch.CS), new BigDecimal("7.0"), 0, List.of("Java"))
        );

        when(jobRepository.findById("job-1")).thenReturn(Optional.of(sampleJob));

        assertThrows(ForbiddenException.class, () -> jobService.updateJob("job-1", request, "other-recruiter"));
    }
}
