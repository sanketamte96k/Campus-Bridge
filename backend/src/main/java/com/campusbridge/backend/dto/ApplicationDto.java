package com.campusbridge.backend.dto;

import com.campusbridge.backend.enums.ApplicationStatus;

public class ApplicationDto {
    private String id;
    private String jobId;
    private String userId;
    private ApplicationStatus status;
    private String appliedAt; // ISO 8601 string

    public ApplicationDto() {
    }

    public ApplicationDto(String id, String jobId, String userId, ApplicationStatus status, String appliedAt) {
        this.id = id;
        this.jobId = jobId;
        this.userId = userId;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(String appliedAt) {
        this.appliedAt = appliedAt;
    }
}
