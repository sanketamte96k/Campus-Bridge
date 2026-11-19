package com.campusbridge.backend.service;

import com.campusbridge.backend.dto.EligibilityCheckDto;
import com.campusbridge.backend.dto.EligibilityResultDto;
import com.campusbridge.backend.dto.EligibilityRuleDto;
import com.campusbridge.backend.dto.StudentProfileDto;
import com.campusbridge.backend.enums.Branch;
import com.campusbridge.backend.enums.EligibilityRuleKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Eligibility Service Tests")
class EligibilityServiceTest {

    private EligibilityService eligibilityService;

    @BeforeEach
    void setUp() {
        eligibilityService = new EligibilityService();
    }

    @Test
    @DisplayName("Should pass eligibility when student meets all criteria")
    void shouldPassWhenAllCriteriaMet() {
        StudentProfileDto profile = new StudentProfileDto(
                Branch.CS,
                8.5,
                0,
                List.of("Java", "Spring Boot", "SQL")
        );

        EligibilityRuleDto rules = new EligibilityRuleDto(
                List.of(Branch.CS, Branch.IT),
                7.5,
                0,
                List.of("Java", "SQL")
        );

        EligibilityResultDto result = eligibilityService.evaluateEligibility(profile, rules);

        assertTrue(result.isEligible());
        assertEquals(4, result.getChecks().size());
        assertTrue(result.getChecks().stream().allMatch(EligibilityCheckDto::isPassed));
    }

    @Test
    @DisplayName("Should fail when branch is not in allowed list")
    void shouldFailWhenBranchNotAllowed() {
        StudentProfileDto profile = new StudentProfileDto(
                Branch.CIVIL,
                8.5,
                0,
                List.of("Java", "SQL")
        );

        EligibilityRuleDto rules = new EligibilityRuleDto(
                List.of(Branch.CS, Branch.IT),
                7.5,
                0,
                List.of("Java")
        );

        EligibilityResultDto result = eligibilityService.evaluateEligibility(profile, rules);

        assertFalse(result.isEligible());
        EligibilityCheckDto branchCheck = result.getChecks().stream()
                .filter(c -> c.getRule() == EligibilityRuleKey.BRANCH)
                .findFirst().orElseThrow();
        assertFalse(branchCheck.isPassed());
        assertTrue(branchCheck.getMessage().contains("Your branch (CIVIL) isn't allowed"));
    }

    @Test
    @DisplayName("Should pass when CGPA exactly equals minimum CGPA requirement")
    void shouldPassWhenCgpaExactlyEqualsMinimum() {
        StudentProfileDto profile = new StudentProfileDto(
                Branch.IT,
                7.0,
                0,
                List.of("React", "TypeScript")
        );

        EligibilityRuleDto rules = new EligibilityRuleDto(
                List.of(Branch.IT),
                7.0,
                0,
                List.of("React")
        );

        EligibilityResultDto result = eligibilityService.evaluateEligibility(profile, rules);

        assertTrue(result.isEligible());
        EligibilityCheckDto cgpaCheck = result.getChecks().stream()
                .filter(c -> c.getRule() == EligibilityRuleKey.CGPA)
                .findFirst().orElseThrow();
        assertTrue(cgpaCheck.isPassed());
        assertEquals("Your CGPA (7.0) meets the minimum of 7.0.", cgpaCheck.getMessage());
    }

    @Test
    @DisplayName("Should fail when CGPA is below minimum requirement")
    void shouldFailWhenCgpaBelowMinimum() {
        StudentProfileDto profile = new StudentProfileDto(
                Branch.IT,
                6.99,
                0,
                List.of("React", "TypeScript")
        );

        EligibilityRuleDto rules = new EligibilityRuleDto(
                List.of(Branch.IT),
                7.0,
                0,
                List.of("React")
        );

        EligibilityResultDto result = eligibilityService.evaluateEligibility(profile, rules);

        assertFalse(result.isEligible());
        EligibilityCheckDto cgpaCheck = result.getChecks().stream()
                .filter(c -> c.getRule() == EligibilityRuleKey.CGPA)
                .findFirst().orElseThrow();
        assertFalse(cgpaCheck.isPassed());
        assertEquals("Your CGPA (6.99) is below the minimum of 7.0.", cgpaCheck.getMessage());
    }

    @Test
    @DisplayName("Should fail when active backlogs exceed maximum allowed")
    void shouldFailWhenBacklogsExceedMaximum() {
        StudentProfileDto profile = new StudentProfileDto(
                Branch.CS,
                8.0,
                2,
                List.of("Java")
        );

        EligibilityRuleDto rules = new EligibilityRuleDto(
                List.of(Branch.CS),
                7.0,
                1,
                List.of("Java")
        );

        EligibilityResultDto result = eligibilityService.evaluateEligibility(profile, rules);

        assertFalse(result.isEligible());
        EligibilityCheckDto backlogCheck = result.getChecks().stream()
                .filter(c -> c.getRule() == EligibilityRuleKey.BACKLOGS)
                .findFirst().orElseThrow();
        assertFalse(backlogCheck.isPassed());
        assertEquals("You have 2 active backlogs. The maximum allowed is 1.", backlogCheck.getMessage());
    }

    @Test
    @DisplayName("Skills matching should be case-insensitive and handle whitespace")
    void shouldMatchSkillsCaseInsensitivelyWithWhitespace() {
        StudentProfileDto profile = new StudentProfileDto(
                Branch.CS,
                8.0,
                0,
                List.of(" java ", "SPRING BOOT", "sql")
        );

        EligibilityRuleDto rules = new EligibilityRuleDto(
                List.of(Branch.CS),
                7.0,
                0,
                List.of("Java", "Spring Boot", "SQL")
        );

        EligibilityResultDto result = eligibilityService.evaluateEligibility(profile, rules);

        assertTrue(result.isEligible());
        EligibilityCheckDto skillsCheck = result.getChecks().stream()
                .filter(c -> c.getRule() == EligibilityRuleKey.SKILLS)
                .findFirst().orElseThrow();
        assertTrue(skillsCheck.isPassed());
        assertEquals("You have all required skills.", skillsCheck.getMessage());
    }

    @Test
    @DisplayName("Should identify missing skills accurately")
    void shouldIdentifyMissingSkills() {
        StudentProfileDto profile = new StudentProfileDto(
                Branch.CS,
                8.0,
                0,
                List.of("Java")
        );

        EligibilityRuleDto rules = new EligibilityRuleDto(
                List.of(Branch.CS),
                7.0,
                0,
                List.of("Java", "Spring Boot", "Docker")
        );

        EligibilityResultDto result = eligibilityService.evaluateEligibility(profile, rules);

        assertFalse(result.isEligible());
        EligibilityCheckDto skillsCheck = result.getChecks().stream()
                .filter(c -> c.getRule() == EligibilityRuleKey.SKILLS)
                .findFirst().orElseThrow();
        assertFalse(skillsCheck.isPassed());
        assertEquals("Missing skills: Spring Boot, Docker.", skillsCheck.getMessage());
    }
}
