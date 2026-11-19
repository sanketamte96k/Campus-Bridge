package com.campusbridge.backend.controller;

import com.campusbridge.backend.dto.ApplicationDto;
import com.campusbridge.backend.dto.StatusUpdateRequest;
import com.campusbridge.backend.exception.BadRequestException;
import com.campusbridge.backend.security.UserPrincipal;
import com.campusbridge.backend.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApplicationDto> applyToJob(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) Map<String, String> body,
            @RequestParam(required = false) String jobId
    ) {
        String targetJobId = jobId;
        if ((targetJobId == null || targetJobId.isBlank()) && body != null) {
            targetJobId = body.get("jobId");
        }

        if (targetJobId == null || targetJobId.isBlank()) {
            throw new BadRequestException("Job ID is required to apply.");
        }

        ApplicationDto application = applicationService.applyToJob(targetJobId, principal.getId());
        return new ResponseEntity<>(application, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ApplicationDto>> getMyApplications(@AuthenticationPrincipal UserPrincipal principal) {
        List<ApplicationDto> applications = applicationService.getMyApplications(principal.getId());
        return ResponseEntity.ok(applications);
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApplicationDto> withdrawApplication(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ApplicationDto application = applicationService.withdrawApplication(id, principal.getId());
        return ResponseEntity.ok(application);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApplicationDto> updateStatusPatch(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        ApplicationDto updated = applicationService.updateApplicationStatus(id, request.getStatus(), principal.getId());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApplicationDto> updateStatusPut(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        ApplicationDto updated = applicationService.updateApplicationStatus(id, request.getStatus(), principal.getId());
        return ResponseEntity.ok(updated);
    }
}
