package com.campusbridge.backend.controller;

import com.campusbridge.backend.dto.AuthResponse;
import com.campusbridge.backend.dto.JobDto;
import com.campusbridge.backend.dto.LoginRequest;
import com.campusbridge.backend.service.JobService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("SkillGap & LearningPlan Controller Security Integration Tests")
class SkillGapControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JobService jobService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    private String studentToken;
    private String recruiterToken;
    private String testJobId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Login as student
        LoginRequest studentLogin = new LoginRequest("student@campusbridge.edu", "Password123!");
        MvcResult studentResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentLogin)))
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse studentAuth = objectMapper.readValue(studentResult.getResponse().getContentAsString(), AuthResponse.class);
        studentToken = "Bearer " + studentAuth.getToken();

        // Login as recruiter
        LoginRequest recruiterLogin = new LoginRequest("recruiter@campusbridge.edu", "Password123!");
        MvcResult recruiterResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recruiterLogin)))
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse recruiterAuth = objectMapper.readValue(recruiterResult.getResponse().getContentAsString(), AuthResponse.class);
        recruiterToken = "Bearer " + recruiterAuth.getToken();

        // Get an existing job ID
        List<JobDto> jobs = jobService.getAllJobs();
        assertFalse(jobs.isEmpty(), "Seed jobs must be present");
        testJobId = jobs.get(0).getId();
    }

    @Test
    @DisplayName("No token returns 401 Unauthorized for both endpoints")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/jobs/{id}/skill-gap", testJobId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/jobs/{id}/learning-plan", testJobId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Recruiter token returns 403 Forbidden for both endpoints")
    void shouldReturn403ForRecruiter() throws Exception {
        mockMvc.perform(get("/api/jobs/{id}/skill-gap", testJobId)
                        .header("Authorization", recruiterToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/jobs/{id}/learning-plan", testJobId)
                        .header("Authorization", recruiterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Student token returns 200 OK for both endpoints")
    void shouldReturn200ForStudent() throws Exception {
        mockMvc.perform(get("/api/jobs/{id}/skill-gap", testJobId)
                        .header("Authorization", studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchedSkills").isArray())
                .andExpect(jsonPath("$.missingSkills").isArray())
                .andExpect(jsonPath("$.cgpaGap").isNumber())
                .andExpect(jsonPath("$.backlogsOver").isNumber())
                .andExpect(jsonPath("$.branchAllowed").isBoolean())
                .andExpect(jsonPath("$.closeToEligible").isBoolean());

        mockMvc.perform(post("/api/jobs/{id}/learning-plan", testJobId)
                        .header("Authorization", studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiGenerated").isBoolean())
                .andExpect(jsonPath("$.disclaimer").isString())
                .andExpect(jsonPath("$.summary").isString())
                .andExpect(jsonPath("$.steps").isArray());
    }
}
