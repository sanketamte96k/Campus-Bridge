package com.campusbridge.backend.dto;

public class JobDto {
    private String id;
    private String title;
    private String company;
    private String location;
    private Double packageLpa;
    private String deadline; // YYYY-MM-DD
    private String description;
    private EligibilityRuleDto rules;
    private String postedBy;

    public JobDto() {
    }

    public JobDto(String id, String title, String company, String location, Double packageLpa,
                  String deadline, String description, EligibilityRuleDto rules, String postedBy) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.packageLpa = packageLpa;
        this.deadline = deadline;
        this.description = description;
        this.rules = rules;
        this.postedBy = postedBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }
}
