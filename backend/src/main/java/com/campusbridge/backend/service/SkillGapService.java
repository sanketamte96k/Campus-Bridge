package com.campusbridge.backend.service;

import com.campusbridge.backend.dto.EligibilityRuleDto;
import com.campusbridge.backend.dto.SkillGapDto;
import com.campusbridge.backend.dto.StudentProfileDto;
import com.campusbridge.backend.enums.Branch;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SkillGapService {

    private static final BigDecimal CLOSE_TO_ELIGIBLE_CGPA_THRESHOLD = new BigDecimal("0.5");
    private static final int CLOSE_TO_ELIGIBLE_MAX_MISSING_SKILLS = 2;

    public SkillGapDto computeGap(StudentProfileDto profile, EligibilityRuleDto rules) {
        if (profile == null || rules == null) {
            throw new IllegalArgumentException("Student profile and eligibility rules must not be null");
        }

        // 1. Skill matching using existing EligibilityService normalization
        Set<String> candidateSkills = EligibilityService.normalizeSkills(profile.getSkills());
        List<String> requiredSkills = rules.getRequiredSkills() != null ? rules.getRequiredSkills() : List.of();

        List<String> matchedSkills = requiredSkills.stream()
                .filter(s -> s != null && candidateSkills.contains(s.trim().toLowerCase()))
                .collect(Collectors.toList());

        List<String> missingSkills = requiredSkills.stream()
                .filter(s -> s != null && !candidateSkills.contains(s.trim().toLowerCase()))
                .collect(Collectors.toList());

        // 2. CGPA gap (0 if met, else minCgpa - studentCgpa)
        BigDecimal studentCgpa = profile.getCgpa() != null ? profile.getCgpa() : BigDecimal.ZERO;
        BigDecimal minCgpa = rules.getMinCgpa() != null ? rules.getMinCgpa() : BigDecimal.ZERO;
        BigDecimal cgpaGap = studentCgpa.compareTo(minCgpa) >= 0
                ? BigDecimal.ZERO
                : minCgpa.subtract(studentCgpa);

        // 3. Backlogs over limit (0 if within the limit)
        int studentBacklogs = profile.getActiveBacklogs() != null ? profile.getActiveBacklogs() : 0;
        int maxBacklogs = rules.getMaxBacklogs() != null ? rules.getMaxBacklogs() : 0;
        int backlogsOver = studentBacklogs <= maxBacklogs
                ? 0
                : studentBacklogs - maxBacklogs;

        // 4. Branch allowed check & required branches list
        boolean branchAllowed = rules.getAllowedBranches() != null
                && rules.getAllowedBranches().contains(profile.getBranch());
        List<Branch> requiredBranches = rules.getAllowedBranches() != null
                ? new ArrayList<>(rules.getAllowedBranches())
                : new ArrayList<>();

        // 5. Close to eligible determination:
        // true when branchAllowed, backlogsOver is 0, cgpaGap <= 0.5, and missingSkills.size() <= 2
        boolean closeToEligible = branchAllowed
                && backlogsOver == 0
                && cgpaGap.compareTo(CLOSE_TO_ELIGIBLE_CGPA_THRESHOLD) <= 0
                && missingSkills.size() <= CLOSE_TO_ELIGIBLE_MAX_MISSING_SKILLS;

        return new SkillGapDto(
                matchedSkills,
                missingSkills,
                cgpaGap,
                backlogsOver,
                branchAllowed,
                requiredBranches,
                closeToEligible
        );
    }
}
