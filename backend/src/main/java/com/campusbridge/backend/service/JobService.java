package com.campusbridge.backend.service;

import com.campusbridge.backend.dto.EligibilityRuleDto;
import com.campusbridge.backend.dto.JobDto;
import com.campusbridge.backend.dto.NewJobRequest;
import com.campusbridge.backend.entity.EligibilityRuleEmbeddable;
import com.campusbridge.backend.entity.Job;
import com.campusbridge.backend.exception.ForbiddenException;
import com.campusbridge.backend.exception.ResourceNotFoundException;
import com.campusbridge.backend.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional(readOnly = true)
    public List<JobDto> getAllJobs() {
        return jobRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobDto getJobById(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        return toDto(job);
    }

    @Transactional(readOnly = true)
    public Job getJobEntity(String id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<JobDto> searchJobs(String query) {
        if (query == null || query.isBlank()) {
            return getAllJobs();
        }
        return jobRepository.searchJobs(query.trim())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobDto> getJobsByRecruiter(String recruiterId) {
        return jobRepository.findByPostedByOrderByCreatedAtDesc(recruiterId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public JobDto createJob(NewJobRequest request, String recruiterId) {
        Job job = new Job();
        job.setTitle(request.getTitle().trim());
        job.setCompany(request.getCompany().trim());
        job.setLocation(request.getLocation().trim());
        job.setPackageLpa(request.getPackageLpa());
        job.setDeadline(parseDate(request.getDeadline()));
        job.setDescription(request.getDescription().trim());
        job.setPostedBy(recruiterId);

        EligibilityRuleEmbeddable rules = new EligibilityRuleEmbeddable();
        if (request.getRules() != null) {
            rules.setAllowedBranches(new ArrayList<>(request.getRules().getAllowedBranches()));
            rules.setMinCgpa(request.getRules().getMinCgpa());
            rules.setMaxBacklogs(request.getRules().getMaxBacklogs());
            rules.setRequiredSkills(new ArrayList<>(request.getRules().getRequiredSkills()));
        }
        job.setRules(rules);

        Job saved = jobRepository.save(job);
        return toDto(saved);
    }

    @Transactional
    public JobDto updateJob(String id, NewJobRequest request, String recruiterId) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        if (job.getPostedBy() != null && !job.getPostedBy().equals(recruiterId)) {
            throw new ForbiddenException("You do not have permission to edit this job.");
        }

        job.setTitle(request.getTitle().trim());
        job.setCompany(request.getCompany().trim());
        job.setLocation(request.getLocation().trim());
        job.setPackageLpa(request.getPackageLpa());
        job.setDeadline(parseDate(request.getDeadline()));
        job.setDescription(request.getDescription().trim());

        EligibilityRuleEmbeddable rules = job.getRules();
        if (rules == null) {
            rules = new EligibilityRuleEmbeddable();
        }
        if (request.getRules() != null) {
            rules.setAllowedBranches(new ArrayList<>(request.getRules().getAllowedBranches()));
            rules.setMinCgpa(request.getRules().getMinCgpa());
            rules.setMaxBacklogs(request.getRules().getMaxBacklogs());
            rules.setRequiredSkills(new ArrayList<>(request.getRules().getRequiredSkills()));
        }
        job.setRules(rules);

        Job saved = jobRepository.save(job);
        return toDto(saved);
    }

    @Transactional
    public void deleteJob(String id, String recruiterId) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        if (job.getPostedBy() != null && !job.getPostedBy().equals(recruiterId)) {
            throw new ForbiddenException("You do not have permission to delete this job.");
        }

        jobRepository.delete(job);
    }

    public JobDto toDto(Job job) {
        if (job == null) {
            return null;
        }

        EligibilityRuleDto rulesDto = null;
        if (job.getRules() != null) {
            rulesDto = new EligibilityRuleDto(
                    new ArrayList<>(job.getRules().getAllowedBranches()),
                    job.getRules().getMinCgpa(),
                    job.getRules().getMaxBacklogs(),
                    new ArrayList<>(job.getRules().getRequiredSkills())
            );
        }

        return new JobDto(
                job.getId(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getPackageLpa(),
                job.getDeadline() != null ? job.getDeadline().toString() : null,
                job.getDescription(),
                rulesDto,
                job.getPostedBy()
        );
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr + ". Expected format: YYYY-MM-DD");
        }
    }
}
