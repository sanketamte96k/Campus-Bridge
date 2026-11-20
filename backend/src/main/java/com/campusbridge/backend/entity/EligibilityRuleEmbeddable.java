package com.campusbridge.backend.entity;

import com.campusbridge.backend.enums.Branch;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class EligibilityRuleEmbeddable {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_allowed_branches", joinColumns = @JoinColumn(name = "job_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "branch", length = 20)
    private List<Branch> allowedBranches = new ArrayList<>();

    @Column(name = "min_cgpa", nullable = false, precision = 4, scale = 2)
    private BigDecimal minCgpa;

    @Column(name = "max_backlogs", nullable = false)
    private Integer maxBacklogs;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_required_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill", length = 100)
    private List<String> requiredSkills = new ArrayList<>();

    public EligibilityRuleEmbeddable() {
    }

    public EligibilityRuleEmbeddable(List<Branch> allowedBranches, BigDecimal minCgpa, Integer maxBacklogs, List<String> requiredSkills) {
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

    public BigDecimal getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(BigDecimal minCgpa) {
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
