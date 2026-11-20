package com.campusbridge.backend.service;

import com.campusbridge.backend.dto.EligibilityCheckDto;
import com.campusbridge.backend.dto.EligibilityResultDto;
import com.campusbridge.backend.dto.EligibilityRuleDto;
import com.campusbridge.backend.dto.StudentProfileDto;
import com.campusbridge.backend.enums.EligibilityRuleKey;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EligibilityService {

    public static Set<String> normalizeSkills(List<String> skills) {
        if (skills == null) {
            return java.util.Collections.emptySet();
        }
        return skills.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.trim().toLowerCase())
                .collect(Collectors.toSet());
    }

    public EligibilityResultDto evaluateEligibility(StudentProfileDto profile, EligibilityRuleDto rules) {
        if (profile == null || rules == null) {
            return new EligibilityResultDto(false, new ArrayList<>());
        }

        List<EligibilityCheckDto> checks = new ArrayList<>();

        // 1. Branch check
        boolean branchPassed = rules.getAllowedBranches() != null &&
                rules.getAllowedBranches().contains(profile.getBranch());
        String allowedBranchesStr = rules.getAllowedBranches() != null ?
                rules.getAllowedBranches().stream().map(Enum::name).collect(Collectors.joining(", ")) : "";
        String branchMessage = branchPassed
                ? "Your branch (" + profile.getBranch() + ") is allowed."
                : "Your branch (" + profile.getBranch() + ") isn't allowed. Allowed: " + allowedBranchesStr + ".";

        checks.add(new EligibilityCheckDto(EligibilityRuleKey.BRANCH, branchPassed, branchMessage));

        // 2. CGPA check (equal passes, compared with compareTo)
        BigDecimal studentCgpa = profile.getCgpa() != null ? profile.getCgpa() : BigDecimal.ZERO;
        BigDecimal minCgpa = rules.getMinCgpa() != null ? rules.getMinCgpa() : BigDecimal.ZERO;
        boolean cgpaPassed = studentCgpa.compareTo(minCgpa) >= 0;
        String cgpaMessage = cgpaPassed
                ? "Your CGPA (" + studentCgpa + ") meets the minimum of " + minCgpa + "."
                : "Your CGPA (" + studentCgpa + ") is below the minimum of " + minCgpa + ".";

        checks.add(new EligibilityCheckDto(EligibilityRuleKey.CGPA, cgpaPassed, cgpaMessage));

        // 3. Backlogs check
        int studentBacklogs = profile.getActiveBacklogs() != null ? profile.getActiveBacklogs() : 0;
        int maxBacklogs = rules.getMaxBacklogs() != null ? rules.getMaxBacklogs() : 0;
        boolean backlogsPassed = studentBacklogs <= maxBacklogs;
        String backlogsMessage = backlogsPassed
                ? "You have " + studentBacklogs + " active backlogs. The maximum is " + maxBacklogs + "."
                : "You have " + studentBacklogs + " active backlogs. The maximum allowed is " + maxBacklogs + ".";

        checks.add(new EligibilityCheckDto(EligibilityRuleKey.BACKLOGS, backlogsPassed, backlogsMessage));

        // 4. Skills check (case-insensitive after trimming, preserves original job skill order/spelling)
        Set<String> candidateSkills = normalizeSkills(profile.getSkills());

        List<String> requiredSkills = rules.getRequiredSkills() != null ? rules.getRequiredSkills() : List.of();
        List<String> missingSkills = requiredSkills.stream()
                .filter(s -> s != null && !candidateSkills.contains(s.trim().toLowerCase()))
                .collect(Collectors.toList());

        boolean skillsPassed = missingSkills.isEmpty();
        String skillsMessage = skillsPassed
                ? "You have all required skills."
                : "Missing skills: " + String.join(", ", missingSkills) + ".";

        checks.add(new EligibilityCheckDto(EligibilityRuleKey.SKILLS, skillsPassed, skillsMessage));

        boolean eligible = checks.stream().allMatch(EligibilityCheckDto::isPassed);

        return new EligibilityResultDto(eligible, checks);
    }
}
