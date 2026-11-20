package com.campusbridge.backend.util;

import java.util.ArrayList;
import java.util.List;

public final class SkillSanitizer {

    public static final int MAX_SKILL_LENGTH = 30;
    public static final int MAX_WORDS = 3;
    public static final int MAX_SKILLS_COUNT = 30;

    private SkillSanitizer() {
    }

    /**
     * Sanitizes an individual skill string:
     * - Allows only letters, digits, spaces, and the characters + # . - /
     * - Trims leading and trailing whitespace and collapses multiple spaces into a single space
     * - Drops the skill entirely (returns null) if longer than 30 characters
     * - Drops the skill entirely (returns null) if more than 3 words after trimming and collapsing spaces
     * - Returns null if the resulting string is empty
     */
    public static String sanitizeSkill(String raw) {
        if (raw == null) {
            return null;
        }

        // Allow only letters, digits, spaces and characters + # . - /
        String cleaned = raw.replaceAll("[^a-zA-Z0-9 +#./-]", "");

        // Trim and collapse whitespace
        cleaned = cleaned.trim().replaceAll("\\s+", " ");

        if (cleaned.isEmpty()) {
            return null;
        }

        // Dropped entirely (not truncated) if longer than 30 characters
        if (cleaned.length() > MAX_SKILL_LENGTH) {
            return null;
        }

        // Dropped entirely if more than 3 words after trimming and collapsing spaces
        String[] words = cleaned.split(" ");
        if (words.length > MAX_WORDS) {
            return null;
        }

        return cleaned;
    }

    /**
     * Sanitizes a list of skills:
     * - Sanitizes each skill
     * - Drops null and empty items
     * - Enforces maximum 30 skills cap
     */
    public static List<String> sanitizeSkills(List<String> skills) {
        if (skills == null) {
            return new ArrayList<>();
        }

        List<String> sanitized = new ArrayList<>();
        for (String skill : skills) {
            String clean = sanitizeSkill(skill);
            if (clean != null && !clean.isBlank()) {
                sanitized.add(clean);
                if (sanitized.size() >= MAX_SKILLS_COUNT) {
                    break;
                }
            }
        }
        return sanitized;
    }

    /**
     * Sanitizes general short non-sensitive text (e.g. job title) for LLM prompts.
     */
    public static String sanitizeText(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String cleaned = text.replaceAll("[^a-zA-Z0-9 +#./-]", " ").trim().replaceAll("\\s+", " ");
        if (cleaned.length() > maxLength) {
            cleaned = cleaned.substring(0, maxLength).trim();
        }
        return cleaned;
    }
}
