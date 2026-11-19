package com.campusbridge.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 150)
    private String company;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(name = "package_lpa", nullable = false)
    private Double packageLpa;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Embedded
    private EligibilityRuleEmbeddable rules;

    @Column(name = "posted_by", length = 36)
    private String postedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Job() {
    }

    public Job(String title, String company, String location, Double packageLpa,
               LocalDate deadline, String description, EligibilityRuleEmbeddable rules, String postedBy) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.packageLpa = packageLpa;
        this.deadline = deadline;
        this.description = description;
        this.rules = rules;
        this.postedBy = postedBy;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EligibilityRuleEmbeddable getRules() {
        return rules;
    }

    public void setRules(EligibilityRuleEmbeddable rules) {
        this.rules = rules;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
