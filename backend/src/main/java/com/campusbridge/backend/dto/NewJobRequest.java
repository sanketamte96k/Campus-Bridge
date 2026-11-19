package com.campusbridge.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class NewJobRequest {

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Company name is required")
    private String company;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Package LPA is required")
    @Positive(message = "Package LPA must be positive")
    private Double packageLpa;

    @NotBlank(message = "Deadline is required (YYYY-MM-DD)")
    private String deadline;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Eligibility rules are required")
    @Valid
    private EligibilityRuleDto rules;

    public NewJobRequest() {
    }

    public NewJobRequest(String title, String company, String location, Double packageLpa,
                         String deadline, String description, EligibilityRuleDto rules) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.packageLpa = packageLpa;
        this.deadline = deadline;
        this.description = description;
        this.rules = rules;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getPackageLpa() {
        return packageLpa;
    }

    public void setPackageLpa(Double packageLpa) {
        this.packageLpa = packageLpa;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EligibilityRuleDto getRules() {
        return rules;
    }

    public void setRules(EligibilityRuleDto rules) {
        this.rules = rules;
    }
}
