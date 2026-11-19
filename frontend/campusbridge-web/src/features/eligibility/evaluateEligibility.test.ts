import { describe, expect, it } from 'vitest';
import type { EligibilityRule, StudentProfile } from '../../types';
import { evaluateEligibility } from './evaluateEligibility';

describe('evaluateEligibility', () => {
  const baseProfile: StudentProfile = {
    branch: 'IT',
    cgpa: 8.5,
    activeBacklogs: 0,
    skills: ['Java', 'SQL', 'Spring Boot'],
  };

  const baseRules: EligibilityRule = {
    allowedBranches: ['IT', 'CS'],
    minCgpa: 7.5,
    maxBacklogs: 0,
    requiredSkills: ['Java', 'SQL', 'Spring Boot'],
  };

  it('passes when all rules pass', () => {
    const result = evaluateEligibility(baseProfile, baseRules);

    expect(result.eligible).toBe(true);
    expect(result.checks.every((c) => c.passed)).toBe(true);
    expect(result.checks[0].message).toBe('Your branch (IT) is allowed.');
    expect(result.checks[1].message).toBe(
      'Your CGPA (8.5) meets the minimum of 7.5.'
    );
    expect(result.checks[2].message).toBe(
      'You have 0 active backlogs. The maximum is 0.'
    );
    expect(result.checks[3].message).toBe('You have all required skills.');
  });

  it('fails when branch is not allowed', () => {
    const profile: StudentProfile = { ...baseProfile, branch: 'MECH' };
    const result = evaluateEligibility(profile, baseRules);

    expect(result.eligible).toBe(false);
    expect(result.checks[0].passed).toBe(false);
    expect(result.checks[0].message).toBe(
      "Your branch (MECH) isn't allowed. Allowed: IT, CS."
    );
  });

  it('passes when CGPA is exactly equal to the minimum', () => {
    const profile: StudentProfile = { ...baseProfile, cgpa: 7.5 };
    const result = evaluateEligibility(profile, baseRules);

    expect(result.eligible).toBe(true);
    expect(result.checks[1].passed).toBe(true);
    expect(result.checks[1].message).toBe(
      'Your CGPA (7.5) meets the minimum of 7.5.'
    );
  });

  it('fails when CGPA is below the minimum', () => {
    const profile: StudentProfile = { ...baseProfile, cgpa: 7.49 };
    const result = evaluateEligibility(profile, baseRules);

    expect(result.eligible).toBe(false);
    expect(result.checks[1].passed).toBe(false);
    expect(result.checks[1].message).toBe(
      'Your CGPA (7.49) is below the minimum of 7.5.'
    );
  });

  it('fails when active backlogs exceed the maximum', () => {
    const profile: StudentProfile = { ...baseProfile, activeBacklogs: 2 };
    const result = evaluateEligibility(profile, baseRules);

    expect(result.eligible).toBe(false);
    expect(result.checks[2].passed).toBe(false);
    expect(result.checks[2].message).toBe(
      'You have 2 active backlogs. The maximum allowed is 0.'
    );
  });

  it('matches skills case-insensitively and ignores surrounding whitespace', () => {
    const profile: StudentProfile = {
      ...baseProfile,
      skills: ['  jAvA  ', 'Sql ', '  SPRING BOOT  '],
    };
    const result = evaluateEligibility(profile, baseRules);

    expect(result.eligible).toBe(true);
    expect(result.checks[3].passed).toBe(true);
    expect(result.checks[3].message).toBe('You have all required skills.');
  });

  it('reports multiple failures and marks eligible as false', () => {
    const profile: StudentProfile = {
      branch: 'CIVIL',
      cgpa: 6.2,
      activeBacklogs: 3,
      skills: ['HTML'],
    };
    const result = evaluateEligibility(profile, baseRules);

    expect(result.eligible).toBe(false);
    expect(result.checks[0].passed).toBe(false);
    expect(result.checks[1].passed).toBe(false);
    expect(result.checks[2].passed).toBe(false);
    expect(result.checks[3].passed).toBe(false);
    expect(result.checks[3].message).toBe(
      'Missing skills: Java, SQL, Spring Boot.'
    );
  });

  it('passes the skills check when requiredSkills list is empty', () => {
    const rulesWithNoSkills: EligibilityRule = {
      ...baseRules,
      requiredSkills: [],
    };
    const profile: StudentProfile = { ...baseProfile, skills: [] };
    const result = evaluateEligibility(profile, rulesWithNoSkills);

    expect(result.eligible).toBe(true);
    expect(result.checks[3].passed).toBe(true);
    expect(result.checks[3].message).toBe('You have all required skills.');
  });

  it('always returns exactly 4 checks in the fixed order: BRANCH, CGPA, BACKLOGS, SKILLS', () => {
    const result = evaluateEligibility(baseProfile, baseRules);

    expect(result.checks).toHaveLength(4);
    expect(result.checks.map((c) => c.rule)).toEqual([
      'BRANCH',
      'CGPA',
      'BACKLOGS',
      'SKILLS',
    ]);
  });

  it('matches the campus user acceptance scenario for student profile', () => {
    const student: StudentProfile = {
      branch: 'IT',
      cgpa: 8.2,
      activeBacklogs: 0,
      skills: ['Java', 'SQL', 'Spring Boot', 'DSA'],
    };

    // 1. Java developer: 6 LPA, Pune, IT/CS, 7.5, 0, Java/SQL/Spring Boot
    const javaDev: EligibilityRule = {
      allowedBranches: ['IT', 'CS'],
      minCgpa: 7.5,
      maxBacklogs: 0,
      requiredSkills: ['Java', 'SQL', 'Spring Boot'],
    };
    expect(evaluateEligibility(student, javaDev).eligible).toBe(true);

    // 2. Frontend engineer: IT/CS/ENTC, 7, 0, React/TypeScript/Tailwind
    const frontendEng: EligibilityRule = {
      allowedBranches: ['IT', 'CS', 'ENTC'],
      minCgpa: 7,
      maxBacklogs: 0,
      requiredSkills: ['React', 'TypeScript', 'Tailwind'],
    };
    expect(evaluateEligibility(student, frontendEng).eligible).toBe(false);

    // 3. Software trainee: IT/CS/ENTC/MECH, 6, 1, Java/DSA
    const softwareTrainee: EligibilityRule = {
      allowedBranches: ['IT', 'CS', 'ENTC', 'MECH'],
      minCgpa: 6,
      maxBacklogs: 1,
      requiredSkills: ['Java', 'DSA'],
    };
    expect(evaluateEligibility(student, softwareTrainee).eligible).toBe(true);

    // 4. Data analyst: IT/CS, 7.5, 0, Python/SQL -> Fails only on skills (missing Python)
    const dataAnalyst: EligibilityRule = {
      allowedBranches: ['IT', 'CS'],
      minCgpa: 7.5,
      maxBacklogs: 0,
      requiredSkills: ['Python', 'SQL'],
    };
    const dataAnalystResult = evaluateEligibility(student, dataAnalyst);
    expect(dataAnalystResult.eligible).toBe(false);
    expect(dataAnalystResult.checks[0].passed).toBe(true);
    expect(dataAnalystResult.checks[1].passed).toBe(true);
    expect(dataAnalystResult.checks[2].passed).toBe(true);
    expect(dataAnalystResult.checks[3].passed).toBe(false);
    expect(dataAnalystResult.checks[3].message).toBe('Missing skills: Python.');

    // 5. Backend engineer: IT/CS, 8, 0, Java/Spring Boot/PostgreSQL/Docker -> Fails only on skills
    const backendEng: EligibilityRule = {
      allowedBranches: ['IT', 'CS'],
      minCgpa: 8,
      maxBacklogs: 0,
      requiredSkills: ['Java', 'Spring Boot', 'PostgreSQL', 'Docker'],
    };
    const backendResult = evaluateEligibility(student, backendEng);
    expect(backendResult.eligible).toBe(false);
    expect(backendResult.checks[0].passed).toBe(true);
    expect(backendResult.checks[1].passed).toBe(true);
    expect(backendResult.checks[2].passed).toBe(true);
    expect(backendResult.checks[3].passed).toBe(false);
    expect(backendResult.checks[3].message).toBe(
      'Missing skills: PostgreSQL, Docker.'
    );
  });
});
