package com.campusbridge.backend.util;

import com.campusbridge.backend.dto.LearningPlanDto;
import com.campusbridge.backend.dto.LearningStepDto;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

public final class PlanOutputValidator {

    public static final String FIXED_DISCLAIMER = "AI-generated suggestions. They do not affect your eligibility.";
    private static final int MAX_STEPS = 6;
    private static final int MAX_STRING_LENGTH = 200;
    private static final int MIN_HOURS = 1;
    private static final int MAX_HOURS = 200;
    private static final int MAX_PRACTICE_IDEAS = 3;

    private static final Set<String> ALLOWED_ROOT_FIELDS = Set.of("summary", "steps");
    private static final Set<String> ALLOWED_STEP_FIELDS = Set.of("skill", "whyItMatters", "practiceIdeas", "estimatedHours");

    private PlanOutputValidator() {
    }

    /**
     * Validates LLM JSON output strictly against safety and schema rules:
     * 1. Rejects malformed JSON
     * 2. Rejects unknown shape / unknown fields
     * 3. Enforces max 6 steps
     * 4. Enforces max string length (200 chars each)
     * 5. Enforces estimatedHours between 1 and 200
     * 6. Drops any step whose skill is not in missingSkills (case-insensitive)
     * 7. Returns Optional.empty() if invalid or no valid steps remain.
     */
    public static Optional<LearningPlanDto> validateAndFilter(
            String jsonOutput,
            List<String> missingSkills,
            ObjectMapper objectMapper
    ) {
        if (jsonOutput == null || jsonOutput.isBlank()) {
            return Optional.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(jsonOutput);
            if (!root.isObject()) {
                return Optional.empty();
            }

            // Reject unknown root fields
            for (String field : root.propertyNames()) {
                if (!ALLOWED_ROOT_FIELDS.contains(field)) {
                    return Optional.empty();
                }
            }

            // Summary validation
            JsonNode summaryNode = root.get("summary");
            if (summaryNode == null || !summaryNode.isTextual()) {
                return Optional.empty();
            }
            String summary = summaryNode.asText().trim();
            if (summary.isEmpty() || summary.length() > MAX_STRING_LENGTH) {
                return Optional.empty();
            }

            // Steps validation
            JsonNode stepsNode = root.get("steps");
            if (stepsNode == null || !stepsNode.isArray() || stepsNode.size() > MAX_STEPS) {
                return Optional.empty();
            }

            // Map missing skills case-insensitively to their original spelling
            Map<String, String> missingSkillsMap = new HashMap<>();
            if (missingSkills != null) {
                for (String ms : missingSkills) {
                    if (ms != null && !ms.isBlank()) {
                        missingSkillsMap.put(ms.trim().toLowerCase(), ms.trim());
                    }
                }
            }

            List<LearningStepDto> validSteps = new ArrayList<>();
            Set<String> seenSkills = new HashSet<>();

            for (JsonNode stepNode : stepsNode) {
                if (!stepNode.isObject()) {
                    return Optional.empty();
                }

                // Reject unknown step fields
                for (String sf : stepNode.propertyNames()) {
                    if (!ALLOWED_STEP_FIELDS.contains(sf)) {
                        return Optional.empty();
                    }
                }

                JsonNode skillNode = stepNode.get("skill");
                JsonNode whyNode = stepNode.get("whyItMatters");
                JsonNode ideasNode = stepNode.get("practiceIdeas");
                JsonNode hoursNode = stepNode.get("estimatedHours");

                if (skillNode == null || !skillNode.isTextual()
                        || whyNode == null || !whyNode.isTextual()
                        || ideasNode == null || !ideasNode.isArray()
                        || hoursNode == null || !hoursNode.isIntegralNumber()) {
                    return Optional.empty();
                }

                String skill = skillNode.asText().trim();
                String whyItMatters = whyNode.asText().trim();
                int estimatedHours = hoursNode.asInt();

                // Length & range validation
                if (skill.isEmpty() || skill.length() > MAX_STRING_LENGTH
                        || whyItMatters.isEmpty() || whyItMatters.length() > MAX_STRING_LENGTH
                        || estimatedHours < MIN_HOURS || estimatedHours > MAX_HOURS) {
                    return Optional.empty();
                }

                // Check practiceIdeas
                if (ideasNode.size() > MAX_PRACTICE_IDEAS) {
                    return Optional.empty();
                }
                List<String> practiceIdeas = new ArrayList<>();
                for (JsonNode ideaNode : ideasNode) {
                    if (!ideaNode.isTextual()) {
                        return Optional.empty();
                    }
                    String idea = ideaNode.asText().trim();
                    if (idea.isEmpty() || idea.length() > MAX_STRING_LENGTH) {
                        return Optional.empty();
                    }
                    practiceIdeas.add(idea);
                }

                // Drop step if skill is not in missingSkills (case-insensitive)
                String normalizedSkill = skill.toLowerCase();
                if (!missingSkillsMap.containsKey(normalizedSkill)) {
                    continue; // drop step
                }

                // Prevent duplicate steps for same skill
                if (seenSkills.contains(normalizedSkill)) {
                    continue;
                }
                seenSkills.add(normalizedSkill);

                // Use the canonical original spelling from missingSkills
                String canonicalSkill = missingSkillsMap.get(normalizedSkill);
                validSteps.add(new LearningStepDto(canonicalSkill, whyItMatters, practiceIdeas, estimatedHours));
            }

            // If missingSkills was non-empty but nothing valid remains, fallback
            if (missingSkills != null && !missingSkills.isEmpty() && validSteps.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new LearningPlanDto(true, FIXED_DISCLAIMER, summary, validSteps));

        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
