package com.campusbridge.backend.dto;

import com.campusbridge.backend.enums.Branch;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class EligibilityRuleDto {

    @NotEmpty(message = "Allowed branches list cannot be empty")
    private List<Branch> allowedBranches = new ArrayList<>();

    @NotNull(message = "Minimum CGPA is required")
    @DecimalMin(value = "0.0", message = "Minimum CGPA must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Minimum CGPA cannot exceed 10.0")
    private Double minCgpa;

    @NotNull(message = "Maximum backlogs is required")
    @Min(value = 0, message = "Maximum backlogs cannot be negative")
    private Integer maxBacklogs;

    @NotNull(message = "Required skills list is required")
    private List<String> requiredSkills = new ArrayList<>();

    public EligibilityRuleDto() {
    }

    public EligibilityRuleDto(List<Branch> allowedBranches, Double minCgpa, Integer maxBacklogs, List<String> requiredSkills) {
        this.allowedBranches = allowedBranches != null ? allowedBranches : new ArrayList<>();
        this.minCgpa = minCgpa;
        this.maxBacklogs = maxBacklogs;
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
    }

    public List<Branch> getAllowedBranches() {
        return allowedBranches;
    }

    public void setAllowedBranches(List<Branch> allowedBranches) {
        this.allowedBranches = allowedBranches != null ? allowedBranches : new ArrayList<>();
    }

    public Double getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(Double minCgpa) {
        this.minCgpa = minCgpa;
    }

    public Integer getMaxBacklogs() {
        return maxBacklogs;
    }

    public void setMaxBacklogs(Integer maxBacklogs) {
        this.maxBacklogs = maxBacklogs;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
    }
}
