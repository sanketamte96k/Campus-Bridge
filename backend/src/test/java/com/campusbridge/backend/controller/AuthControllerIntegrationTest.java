package com.campusbridge.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Auth & Job Controller Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /api/jobs should be publicly accessible without authentication")
    void shouldAllowPublicJobListing() throws Exception {
        mockMvc.perform(get("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/auth/register should create student user and return JWT token")
    void shouldRegisterNewUserViaApi() throws Exception {
        String uniqueEmail = "test_" + UUID.randomUUID().toString().substring(0, 8) + "@campusbridge.edu";
        Map<String, Object> registerRequest = Map.of(
                "name", "Test Candidate",
                "email", uniqueEmail,
                "password", "SecurePass123!",
                "role", "STUDENT"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("POST /api/auth/login with valid seeded user should return JWT token")
    void shouldLoginSeededUser() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "email", "student@campusbridge.edu",
                "password", "Password123!"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value("student@campusbridge.edu"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("POST /api/jobs without recruiter credentials should return 401 or 403")
    void shouldDenyUnauthenticatedJobCreation() throws Exception {
        Map<String, Object> jobRequest = Map.of(
                "title", "Unauthorized Job",
                "company", "Test Corp",
                "location", "Remote",
                "packageLpa", 12.0,
                "deadline", "2026-12-31",
                "description", "Should fail"
        );

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isForbidden());
    }
}
