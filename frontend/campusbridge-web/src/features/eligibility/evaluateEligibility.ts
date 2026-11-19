import type {
  EligibilityCheck,
  EligibilityResult,
  EligibilityRule,
  StudentProfile,
} from '../../types';

export function evaluateEligibility(
  profile: StudentProfile,
  rules: EligibilityRule
): EligibilityResult {
  // 1. Branch check
  const branchPassed = rules.allowedBranches.includes(profile.branch);
  const branchCheck: EligibilityCheck = {
    rule: 'BRANCH',
    passed: branchPassed,
    message: branchPassed
      ? `Your branch (${profile.branch}) is allowed.`
      : `Your branch (${profile.branch}) isn't allowed. Allowed: ${rules.allowedBranches.join(', ')}.`,
  };

  // 2. CGPA check (equal passes)
  const cgpaPassed = profile.cgpa >= rules.minCgpa;
  const cgpaCheck: EligibilityCheck = {
    rule: 'CGPA',
    passed: cgpaPassed,
    message: cgpaPassed
      ? `Your CGPA (${profile.cgpa}) meets the minimum of ${rules.minCgpa}.`
      : `Your CGPA (${profile.cgpa}) is below the minimum of ${rules.minCgpa}.`,
  };

  // 3. Backlogs check
  const backlogsPassed = profile.activeBacklogs <= rules.maxBacklogs;
  const backlogsCheck: EligibilityCheck = {
    rule: 'BACKLOGS',
    passed: backlogsPassed,
    message: backlogsPassed
      ? `You have ${profile.activeBacklogs} active backlogs. The maximum is ${rules.maxBacklogs}.`
      : `You have ${profile.activeBacklogs} active backlogs. The maximum allowed is ${rules.maxBacklogs}.`,
  };

  // 4. Skills check (case-insensitive after trimming, preserves original job skill order/spelling)
  const candidateSkills = new Set(
    profile.skills.map((skill) => skill.trim().toLowerCase())
  );
  const missingSkills = rules.requiredSkills.filter(
    (requiredSkill) => !candidateSkills.has(requiredSkill.trim().toLowerCase())
  );
  const skillsPassed = missingSkills.length === 0;
  const skillsCheck: EligibilityCheck = {
    rule: 'SKILLS',
    passed: skillsPassed,
    message: skillsPassed
      ? 'You have all required skills.'
      : `Missing skills: ${missingSkills.join(', ')}.`,
  };

  const checks: EligibilityCheck[] = [
    branchCheck,
    cgpaCheck,
    backlogsCheck,
    skillsCheck,
  ];

  return {
    eligible: checks.every((check) => check.passed),
    checks,
  };
}
