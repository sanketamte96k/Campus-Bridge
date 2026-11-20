package com.campusbridge.backend.service;

import com.campusbridge.backend.dto.EligibilityRuleDto;
import com.campusbridge.backend.dto.SkillGapDto;
import com.campusbridge.backend.dto.StudentProfileDto;
import com.campusbridge.backend.enums.Branch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SkillGapService Tests")
class SkillGapServiceTest {

    private SkillGapService skillGapService;

    @BeforeEach
    void setUp() {
        skillGapService = new SkillGapService();
    }

    @Test
    @DisplayName("All criteria met: matchedSkills contains all, missingSkills is empty, gap is zero, closeToEligible is true")
    void shouldComputeGapWhenAllCriteriaMet() {
        StudentProfileDto profile = new StudentProfileDto(
                Branch.CS,
                new BigDecimal("8.50"),
                0,
                List.of("Java", "Spring Boot", "SQL")
        );

        EligibilityRuleDto rules = new EligibilityRuleDto(
                List.of(Branch.CS, Branch.IT),
                new BigDecimal("7.50"),
                0,
                List.of("Java", "SQL")
        );

        SkillGapDto gap = skillGapService.computeGap(profile, rules);

        assertTrue(gap.isBranchAllowed());
        assertEquals(0, gap.getBacklogsOver());
        assertEquals(0, gap.getCgpaGap().compareTo(BigDecimal.ZERO));
        assertEquals(List.of("Java", "SQL"), gap.getMatchedSkills());
        assertTrue(gap.getMissingSkills().isEmpty());
        assertTrue(gap.isCloseToEligible());
    }

    @Test
    @DisplayName("Skill matching handles case-insensitivity and whitespace differences while preserving original job spelling and order")
    void shouldMatchSkillsCaseInsensitivelyAndPreserveJobSpelling() {
        StudentProfileDto profile = new StudentProfileDto(
                Branch.IT,
                new BigDecimal("7.80"),
                0,
                List.of("  java  ", "SPRING BOOT", "Docker")
        );

        EligibilityRuleDto rules = new EligibilityRuleDto(
                List.of(Branch.IT),
                new BigDecimal("7.00"),
                1,
                List.of("Java", "Kubernetes", "Spring Boot", "AWS")
        );

        SkillGapDto gap = skillGapService.computeGap(profile, rules);

        assertEquals(List.of("Java", "Spring Boot"), gap.getMatchedSkills());
        assertEquals(List.of("Kubernetes", "AWS"), gap.getMissingSkills());
        assertTrue(gap.isCloseToEligible()); // missing 2 skills, branch allowed, backlogs=0, cgpaGap=0
    }

    @Test
    @DisplayName("CGPA gap computation at exact boundary (minCgpa equals studentCgpa)")
    void shouldHandleCgpaGapAtExactBoundary() {
        StudentProfileDto profile = new StudentProfileDto(
                Branch.CS,
                new BigDecimal("7.50"),
                0,
                List.of("Java")
        );

        EligibilityRuleDto rules = new EligibilityRuleDto(
                List.of(Branch.CS),
                new BigDecimal("7.50"),
                0,
                List.of("Java")
        );

        SkillGapDto gap = skillGapService.computeGap(profile, rules);

        assertEquals(0, gap.getCgpaGap().compareTo(BigDecimal.ZERO));
        assertTrue(gap.isCloseToEligible());
    }

    @Test
    @DisplayName("CGPA gap of exactly 0.50 maintains closeToEligible true, while 0.51 makes it false")
    void shouldEvaluateCloseToEligibleAtCgpaBoundary() {
        // Exactly 0.50 gap
        StudentProfileDto profileBoundary = new StudentProfileDto(
                Branch.CS,
                new BigDecimal("7.00"),
                0,
                List.of("Java")
        );
        EligibilityRuleDto rules = new EligibilityRuleDto(
                List.of(Branch.CS),
                new BigDecimal("7.50"),
                0,
                List.of("Java")
        );

        SkillGapDto gapBoundary = skillGapService.computeGap(profileBoundary, rules);
        assertEquals(0, gapBoundary.getCgpaGap().compareTo(new BigDecimal("0.50")));
        assertTrue(gapBoundary.isCloseToEligible());

        // 0.51 gap
        StudentProfileDto profileOver = new StudentProfileDto(
                Branch.CS,
                new BigDecimal("6.99"),
                0,
                List.of("Java")
        );
        SkillGapDto gapOver = skillGapService.computeGap(profileOver, rules);
        assertEquals(0, gapOver.getCgpaGap().compareTo(new BigDecimal("0.51")));
        assertFalse(gapOver.isCloseToEligible());
    }

    @Test
    @DisplayName("Backlogs over limit calculates backlogsOver and invalidates closeToEligible")
    void shouldCalculateBacklogsOverLimit() {
        StudentProfileDto profile = new StudentProfileDto(
                Branch.CS,
                new BigDecimal("8.00"),
                3,
                List.of("Java")
        );

        EligibilityRuleDto rules = new EligibilityRuleDto(
                List.of(Branch.CS),
                new BigDecimal("7.00"),
                1,
                List.of("Java")
        );

        SkillGapDto gap = skillGapService.computeGap(profile, rules);

        assertEquals(2, gap.getBacklogsOver());
        assertFalse(gap.isCloseToEligible()); // backlogsOver > 0 disqualifies closeToEligible
    }

    @Test
    @DisplayName("Disallowed branch sets branchAllowed false and invalidates closeToEligible")
    void shouldHandleDisallowedBranch() {
        StudentProfileDto profile = new StudentProfileDto(
                Branch.MECH,
                new BigDecimal("9.00"),
                0,
                List.of("Java", "Spring Boot")
        );

        EligibilityRuleDto rules = new EligibilityRuleDto(
                List.of(Branch.CS, Branch.IT),
                new BigDecimal("7.00"),
                0,
                List.of("Java", "Spring Boot")
        );

        SkillGapDto gap = skillGapService.computeGap(profile, rules);

        assertFalse(gap.isBranchAllowed());
        assertEquals(List.of(Branch.CS, Branch.IT), gap.getRequiredBranches());
        assertFalse(gap.isCloseToEligible()); // branch disallowed disqualifies closeToEligible
    }

    @Test
    @DisplayName("CloseToEligible is false when missing more than 2 skills even if other criteria are met")
    void shouldRejectCloseToEligibleWhenMissingMoreThanTwoSkills() {
        StudentProfileDto profile = new StudentProfileDto(
                Branch.CS,
                new BigDecimal("8.00"),
                0,
                List.of("Java")
        );

        EligibilityRuleDto rules = new EligibilityRuleDto(
                List.of(Branch.CS),
                new BigDecimal("7.00"),
                0,
                List.of("Java", "Docker", "Kubernetes", "AWS")
        );

        SkillGapDto gap = skillGapService.computeGap(profile, rules);

        assertEquals(3, gap.getMissingSkills().size());
        assertFalse(gap.isCloseToEligible());
    }
}
