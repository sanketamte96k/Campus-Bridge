package com.campusbridge.backend.service;

import com.campusbridge.backend.dto.AuthResponse;
import com.campusbridge.backend.dto.LoginRequest;
import com.campusbridge.backend.dto.RegisterRequest;
import com.campusbridge.backend.dto.UserDto;
import com.campusbridge.backend.entity.User;
import com.campusbridge.backend.exception.BadRequestException;
import com.campusbridge.backend.exception.ResourceNotFoundException;
import com.campusbridge.backend.repository.UserRepository;
import com.campusbridge.backend.security.JwtService;
import com.campusbridge.backend.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("An account with this email already exists.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(savedUser);
        String token = jwtService.generateToken(principal);

        UserDto userDto = new UserDto(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getRole());
        return new AuthResponse(token, userDto);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);

            UserDto userDto = new UserDto(principal.getId(), principal.getName(), principal.getUsername(), principal.getRole());
            return new AuthResponse(token, userDto);
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Email or password is incorrect.");
        }
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    @Transactional(readOnly = true)
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
