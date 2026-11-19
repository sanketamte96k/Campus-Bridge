package com.campusbridge.backend.controller;

import com.campusbridge.backend.dto.UserDto;
import com.campusbridge.backend.entity.User;
import com.campusbridge.backend.security.UserPrincipal;
import com.campusbridge.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        UserDto user = authService.getCurrentUser(principal.getUsername());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        User user = authService.getUserById(id);
        return ResponseEntity.ok(new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole()));
    }
}
