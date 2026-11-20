package com.campusbridge.backend.service;

import com.campusbridge.backend.client.LlmClient;
import com.campusbridge.backend.dto.LearningPlanDto;
import com.campusbridge.backend.security.PlanRateLimiter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("LearningPlanService Unit Tests")
class LearningPlanServiceTest {

    private LlmClient mockLlmClient;
    private PlanRateLimiter rateLimiter;
    private PlanCache planCache;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockLlmClient = mock(LlmClient.class);
        rateLimiter = new PlanRateLimiter();
        planCache = new PlanCache();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Successful LLM call returns validated AI plan with aiGenerated=true")
    void shouldReturnAiGeneratedPlanOnSuccess() {
        String validJson = """
                {
                  "summary": "Solid cloud roadmap.",
                  "steps": [
                    {
                      "skill": "Docker",
                      "whyItMatters": "Container runtime.",
                      "practiceIdeas": ["Containerize app"],
                      "estimatedHours": 12
                    }
                  ]
                }
                """;
        when(mockLlmClient.generate(anyString(), anyString())).thenReturn(validJson);

        LearningPlanService service = new LearningPlanService(
                mockLlmClient,
                rateLimiter,
                planCache,
                objectMapper,
                "valid-test-gemini-key"
        );

        LearningPlanDto plan = service.generatePlan(
                "user-123",
                "job-456",
                "Backend Engineer",
                List.of("Java", "Docker"),
                List.of("Docker"),
                List.of("Java")
        );

        assertTrue(plan.isAiGenerated());
        assertEquals("Solid cloud roadmap.", plan.getSummary());
        assertEquals(1, plan.getSteps().size());
        assertEquals("Docker", plan.getSteps().get(0).getSkill());
        verify(mockLlmClient, times(1)).generate(anyString(), anyString());
    }

    @Test
    @DisplayName("Exception or timeout in LlmClient returns deterministic fallback with aiGenerated=false")
    void shouldReturnFallbackPlanOnLlmFailure() {
        when(mockLlmClient.generate(anyString(), anyString()))
                .thenThrow(new RuntimeException("Connection timed out after 15000ms"));

        LearningPlanService service = new LearningPlanService(
                mockLlmClient,
                rateLimiter,
                planCache,
                objectMapper,
                "valid-test-gemini-key"
        );

        LearningPlanDto plan = service.generatePlan(
                "user-123",
                "job-456",
                "Backend Engineer",
                List.of("Java", "Docker"),
                List.of("Docker"),
                List.of("Java")
        );

        assertFalse(plan.isAiGenerated());
        assertEquals("AI-generated suggestions. They do not affect your eligibility.", plan.getDisclaimer());
        assertEquals(1, plan.getSteps().size());
        assertEquals("Docker", plan.getSteps().get(0).getSkill());
        assertEquals(10, plan.getSteps().get(0).getEstimatedHours());
    }

    @Test
    @DisplayName("Blank API key never invokes LlmClient and returns fallback immediately")
    void shouldNeverCallLlmWhenApiKeyIsBlank() {
        LearningPlanService serviceWithBlankKey = new LearningPlanService(
                mockLlmClient,
                rateLimiter,
                planCache,
                objectMapper,
                "   "
        );

        LearningPlanDto plan = serviceWithBlankKey.generatePlan(
                "user-123",
                "job-456",
                "Backend Engineer",
                List.of("Java", "Docker"),
                List.of("Docker"),
                List.of("Java")
        );

        assertFalse(plan.isAiGenerated());
        assertEquals(1, plan.getSteps().size());
        assertEquals("Docker", plan.getSteps().get(0).getSkill());
        verifyNoInteractions(mockLlmClient);
    }

    @Test
    @DisplayName("Subsequent request with identical inputs hits memory cache and doesn't call LlmClient twice")
    void shouldServeFromCacheOnSecondCall() {
        String validJson = """
                {
                  "summary": "Cloud roadmap.",
                  "steps": [
                    {
                      "skill": "Docker",
                      "whyItMatters": "Containers.",
                      "practiceIdeas": ["Containerize app"],
                      "estimatedHours": 10
                    }
                  ]
                }
                """;
        when(mockLlmClient.generate(anyString(), anyString())).thenReturn(validJson);

        LearningPlanService service = new LearningPlanService(
                mockLlmClient,
                rateLimiter,
                planCache,
                objectMapper,
                "valid-test-gemini-key"
        );

        // First call
        LearningPlanDto plan1 = service.generatePlan(
                "user-123",
                "job-456",
                "Backend Engineer",
                List.of("Java", "Docker"),
                List.of("Docker"),
                List.of("Java")
        );

        // Second call (cache hit)
        LearningPlanDto plan2 = service.generatePlan(
                "user-123",
                "job-456",
                "Backend Engineer",
                List.of("Java", "Docker"),
                List.of("Docker"),
                List.of("Java")
        );

        assertTrue(plan1.isAiGenerated());
        assertTrue(plan2.isAiGenerated());
        assertEquals(plan1.getSummary(), plan2.getSummary());
        // Verify LLM client was called exactly ONCE
        verify(mockLlmClient, times(1)).generate(anyString(), anyString());
    }

    @Test
    @DisplayName("Payload sent to LLM contains ONLY jobTitle and sanitized skills; NEVER personal data")
    void shouldNeverSendPersonalDataToLlm() throws Exception {
        String validJson = """
                {
                  "summary": "Plan summary.",
                  "steps": [
                    { "skill": "Docker", "whyItMatters": "Reason", "practiceIdeas": ["Idea"], "estimatedHours": 10 }
                  ]
                }
                """;
        when(mockLlmClient.generate(anyString(), anyString())).thenReturn(validJson);

        LearningPlanService service = new LearningPlanService(
                mockLlmClient,
                rateLimiter,
                planCache,
                objectMapper,
                "valid-test-gemini-key"
        );

        service.generatePlan(
                "user-alex-rivera-id",
                "job-456",
                "Senior Backend Engineer",
                List.of("Java", "Docker"),
                List.of("Docker"),
                List.of("Java")
        );

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockLlmClient).generate(anyString(), payloadCaptor.capture());

        String capturedJson = payloadCaptor.getValue();
        JsonNode payload = objectMapper.readTree(capturedJson);

        // Assert strictly allowed keys
        assertTrue(payload.has("jobTitle"));
        assertTrue(payload.has("requiredSkills"));
        assertTrue(payload.has("missingSkills"));
        assertTrue(payload.has("studentSkills"));

        // Assert NO personal data or identifiers
        assertFalse(payload.has("name"));
        assertFalse(payload.has("email"));
        assertFalse(payload.has("id"));
        assertFalse(payload.has("userId"));
        assertFalse(payload.has("studentId"));
        assertFalse(payload.has("cgpa"));
        assertFalse(payload.has("branch"));
        assertFalse(capturedJson.contains("user-alex-rivera-id"));
    }

    @Test
    @DisplayName("Mockito ArgumentCaptor verifies JSON sent to LlmClient contains none of student name, email, user id, CGPA")
    void shouldVerifyJsonSentToLlmContainsNoNameEmailUserIdOrCgpa() {
        String validJson = """
                {
                  "summary": "Plan summary.",
                  "steps": [
                    { "skill": "Docker", "whyItMatters": "Reason", "practiceIdeas": ["Idea"], "estimatedHours": 10 }
                  ]
                }
                """;
        when(mockLlmClient.generate(anyString(), anyString())).thenReturn(validJson);

        LearningPlanService service = new LearningPlanService(
                mockLlmClient,
                rateLimiter,
                planCache,
                objectMapper,
                "valid-test-gemini-key"
        );

        String studentName = "Alex Rivera";
        String studentEmail = "alex.rivera@campusbridge.edu";
        String studentUserId = "user-uuid-9876";
        String studentCgpa = "8.75";

        service.generatePlan(
                studentUserId,
                "job-backend-789",
                "Backend Engineer",
                List.of("Java", "Docker"),
                List.of("Docker"),
                List.of("Java")
        );

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockLlmClient, atLeastOnce()).generate(promptCaptor.capture(), jsonCaptor.capture());

        String capturedJson = jsonCaptor.getValue();
        assertNotNull(capturedJson);

        // Verify JSON sent to LlmClient contains none of: the student's name, email, user id, CGPA
        assertFalse(capturedJson.toLowerCase().contains("alex"), "JSON must not contain student first name");
        assertFalse(capturedJson.toLowerCase().contains("rivera"), "JSON must not contain student last name");
        assertFalse(capturedJson.toLowerCase().contains(studentName.toLowerCase()), "JSON must not contain student full name");
        assertFalse(capturedJson.toLowerCase().contains(studentEmail.toLowerCase()), "JSON must not contain student email");
        assertFalse(capturedJson.toLowerCase().contains("@"), "JSON must not contain email symbol");
        assertFalse(capturedJson.toLowerCase().contains(studentUserId.toLowerCase()), "JSON must not contain student user id");
        assertFalse(capturedJson.toLowerCase().contains("9876"), "JSON must not contain student user id fragment");
        assertFalse(capturedJson.toLowerCase().contains("cgpa"), "JSON must not contain CGPA field");
        assertFalse(capturedJson.contains(studentCgpa), "JSON must not contain student CGPA value");
    }
}
