package com.campusbridge.backend.entity;

import com.campusbridge.backend.enums.ApplicationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "applications", indexes = {
        @Index(name = "idx_applications_job_id", columnList = "job_id"),
        @Index(name = "idx_applications_user_id", columnList = "user_id")
})
public class Application {

    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "job_id", nullable = false, length = 36)
    private String jobId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    public Application() {
    }

    public Application(String jobId, String userId, ApplicationStatus status) {
        this.jobId = jobId;
        this.userId = userId;
        this.status = status != null ? status : ApplicationStatus.APPLIED;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.appliedAt == null) {
            this.appliedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ApplicationStatus.APPLIED;
        }
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

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}
