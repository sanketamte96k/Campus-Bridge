package com.campusbridge.backend.service;

import com.campusbridge.backend.dto.AuthResponse;
import com.campusbridge.backend.dto.LoginRequest;
import com.campusbridge.backend.dto.RegisterRequest;
import com.campusbridge.backend.entity.User;
import com.campusbridge.backend.enums.Role;
import com.campusbridge.backend.exception.BadRequestException;
import com.campusbridge.backend.repository.UserRepository;
import com.campusbridge.backend.security.JwtService;
import com.campusbridge.backend.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("usr-1", "John Doe", "john@campusbridge.edu", "encodedPassword", Role.STUDENT);
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void shouldRegisterNewUser() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@campusbridge.edu", "Password123!", Role.STUDENT);

        when(userRepository.existsByEmail("john@campusbridge.edu")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals("john@campusbridge.edu", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException if email already registered")
    void shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@campusbridge.edu", "Password123!", Role.STUDENT);

        when(userRepository.existsByEmail("john@campusbridge.edu")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.register(request));
        assertEquals("An account with this email already exists.", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully login user with valid credentials")
    void shouldLoginWithValidCredentials() {
        LoginRequest request = new LoginRequest("john@campusbridge.edu", "Password123!");
        UserPrincipal principal = new UserPrincipal(sampleUser);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generateToken(principal)).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("john@campusbridge.edu", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());
    }

    @Test
    @DisplayName("Should throw BadRequestException if login credentials are bad")
    void shouldThrowWhenLoginCredentialsInvalid() {
        LoginRequest request = new LoginRequest("john@campusbridge.edu", "WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.login(request));
        assertEquals("Email or password is incorrect.", ex.getMessage());
    }
}
