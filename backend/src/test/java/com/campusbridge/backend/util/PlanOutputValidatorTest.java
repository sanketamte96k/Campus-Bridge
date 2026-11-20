package com.campusbridge.backend.util;

import com.campusbridge.backend.dto.LearningPlanDto;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlanOutputValidator Tests")
class PlanOutputValidatorTest {

    private ObjectMapper objectMapper;
    private final List<String> missingSkills = List.of("Docker", "Kubernetes", "AWS");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Accepts valid JSON matching schema and missing skills")
    void shouldAcceptValidPlanJson() {
        String validJson = """
                {
                  "summary": "Master containerization and cloud orchestration.",
                  "steps": [
                    {
                      "skill": "Docker",
                      "whyItMatters": "Containerization standard for scalable backend microservices.",
                      "practiceIdeas": ["Containerize a Spring Boot app", "Write a multi-stage Dockerfile"],
                      "estimatedHours": 15
                    },
                    {
                      "skill": "kubernetes",
                      "whyItMatters": "Orchestrates multi-container clusters in production.",
                      "practiceIdeas": ["Deploy pods and services in Minikube", "Configure ingress"],
                      "estimatedHours": 25
                    }
                  ]
                }
                """;

        Optional<LearningPlanDto> result = PlanOutputValidator.validateAndFilter(validJson, missingSkills, objectMapper);

        assertTrue(result.isPresent());
        LearningPlanDto plan = result.get();
        assertTrue(plan.isAiGenerated());
        assertEquals("Master containerization and cloud orchestration.", plan.getSummary());
        assertEquals(2, plan.getSteps().size());
        assertEquals("Docker", plan.getSteps().get(0).getSkill());
        assertEquals("Kubernetes", plan.getSteps().get(1).getSkill()); // original spelling preserved
        assertEquals(15, plan.getSteps().get(0).getEstimatedHours());
        assertEquals(25, plan.getSteps().get(1).getEstimatedHours());
    }

    @Test
    @DisplayName("Rejects malformed JSON syntax")
    void shouldRejectMalformedJson() {
        String malformedJson = "{ summary: not valid json at all ...";
        Optional<LearningPlanDto> result = PlanOutputValidator.validateAndFilter(malformedJson, missingSkills, objectMapper);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Rejects extra unknown fields on root or step")
    void shouldRejectExtraFields() {
        String unknownRootField = """
                {
                  "summary": "Good summary",
                  "extraField": "surprise injection",
                  "steps": []
                }
                """;
        Optional<LearningPlanDto> result = PlanOutputValidator.validateAndFilter(unknownRootField, missingSkills, objectMapper);
        assertFalse(result.isPresent());

        String unknownStepField = """
                {
                  "summary": "Good summary",
                  "steps": [
                    {
                      "skill": "Docker",
                      "whyItMatters": "Containers",
                      "practiceIdeas": ["Build an image"],
                      "estimatedHours": 10,
                      "unrecognizedProperty": "bad"
                    }
                  ]
                }
                """;
        Optional<LearningPlanDto> stepResult = PlanOutputValidator.validateAndFilter(unknownStepField, missingSkills, objectMapper);
        assertFalse(stepResult.isPresent());
    }

    @Test
    @DisplayName("Rejects more than 6 steps")
    void shouldRejectTooManySteps() {
        String tooManySteps = """
                {
                  "summary": "Extensive plan",
                  "steps": [
                    { "skill": "Docker", "whyItMatters": "a", "practiceIdeas": ["b"], "estimatedHours": 10 },
                    { "skill": "Docker", "whyItMatters": "a", "practiceIdeas": ["b"], "estimatedHours": 10 },
                    { "skill": "Docker", "whyItMatters": "a", "practiceIdeas": ["b"], "estimatedHours": 10 },
                    { "skill": "Docker", "whyItMatters": "a", "practiceIdeas": ["b"], "estimatedHours": 10 },
                    { "skill": "Docker", "whyItMatters": "a", "practiceIdeas": ["b"], "estimatedHours": 10 },
                    { "skill": "Docker", "whyItMatters": "a", "practiceIdeas": ["b"], "estimatedHours": 10 },
                    { "skill": "Docker", "whyItMatters": "a", "practiceIdeas": ["b"], "estimatedHours": 10 }
                  ]
                }
                """;
        Optional<LearningPlanDto> result = PlanOutputValidator.validateAndFilter(tooManySteps, missingSkills, objectMapper);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Rejects out of range estimatedHours (< 1 or > 200)")
    void shouldRejectOutOfRangeEstimatedHours() {
        String zeroHours = """
                {
                  "summary": "Good summary",
                  "steps": [
                    { "skill": "Docker", "whyItMatters": "Why", "practiceIdeas": ["Idea"], "estimatedHours": 0 }
                  ]
                }
                """;
        assertFalse(PlanOutputValidator.validateAndFilter(zeroHours, missingSkills, objectMapper).isPresent());

        String overHours = """
                {
                  "summary": "Good summary",
                  "steps": [
                    { "skill": "Docker", "whyItMatters": "Why", "practiceIdeas": ["Idea"], "estimatedHours": 201 }
                  ]
                }
                """;
        assertFalse(PlanOutputValidator.validateAndFilter(overHours, missingSkills, objectMapper).isPresent());
    }

    @Test
    @DisplayName("Drops steps for skills that were not in missingSkills")
    void shouldDropStepForSkillThatIsNotMissing() {
        String jsonWithHallucinatedSkill = """
                {
                  "summary": "Learning plan with extra skill",
                  "steps": [
                    {
                      "skill": "React",
                      "whyItMatters": "Frontend UI",
                      "practiceIdeas": ["Build component"],
                      "estimatedHours": 10
                    },
                    {
                      "skill": "Docker",
                      "whyItMatters": "Microservices",
                      "practiceIdeas": ["Run container"],
                      "estimatedHours": 10
                    }
                  ]
                }
                """;

        // React is NOT in missingSkills [Docker, Kubernetes, AWS]
        Optional<LearningPlanDto> result = PlanOutputValidator.validateAndFilter(jsonWithHallucinatedSkill, missingSkills, objectMapper);

        assertTrue(result.isPresent());
        LearningPlanDto plan = result.get();
        assertEquals(1, plan.getSteps().size());
        assertEquals("Docker", plan.getSteps().get(0).getSkill());
    }
}
