package com.campusbridge.backend.dto;

import com.campusbridge.backend.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(ApplicationStatus status) {
        this.status = status;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}
