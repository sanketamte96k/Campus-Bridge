package com.campusbridge.backend.controller;

import com.campusbridge.backend.dto.StudentProfileDto;
import com.campusbridge.backend.security.UserPrincipal;
import com.campusbridge.backend.service.StudentProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentProfileService profileService;

    public StudentController(StudentProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileDto> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        StudentProfileDto profile = profileService.getProfileByUserId(principal.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileDto> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StudentProfileDto dto
    ) {
        StudentProfileDto saved = profileService.saveProfile(principal.getId(), dto);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileDto> saveMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StudentProfileDto dto
    ) {
        StudentProfileDto saved = profileService.saveProfile(principal.getId(), dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<StudentProfileDto> getProfileByUserId(@PathVariable String userId) {
        StudentProfileDto profile = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }
}
