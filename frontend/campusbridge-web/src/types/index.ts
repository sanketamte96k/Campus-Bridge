export type Role = 'STUDENT' | 'RECRUITER';

export interface User {
  id: string;
  name: string;
  email: string;
  role: Role;
}

export interface LoginInput {
  email: string;
  password: string;
}

export interface RegisterInput {
  name: string;
  email: string;
  password: string;
  role: Role;
}

export type Branch = 'CS' | 'IT' | 'ENTC' | 'MECH' | 'CIVIL';

export interface EligibilityRule {
  allowedBranches: Branch[];
  minCgpa: number;
  maxBacklogs: number;
  requiredSkills: string[];
}

export interface Job {
  id: string;
  title: string;
  company: string;
  location: string;
  packageLpa: number;
  deadline: string; // ISO date, e.g. "2026-12-15"
  description: string;
  rules: EligibilityRule;
  postedBy?: string;
}

export type NewJobInput = Omit<Job, 'id' | 'postedBy'>;

export type ApplicationStatus =
  | 'APPLIED'
  | 'SHORTLISTED'
  | 'SELECTED'
  | 'REJECTED'
  | 'WITHDRAWN';

export interface Application {
  id: string;
  jobId: string;
  userId: string;
  status: ApplicationStatus;
  appliedAt: string; // ISO datetime
}

export interface StudentProfile {
  branch: Branch;
  cgpa: number;
  activeBacklogs: number;
  skills: string[];
}

export interface Applicant {
  application: Application;
  name: string;
  email: string;
  profile: StudentProfile | null;
}

export type EligibilityRuleKey = 'BRANCH' | 'CGPA' | 'BACKLOGS' | 'SKILLS';

export interface EligibilityCheck {
  rule: EligibilityRuleKey;
  passed: boolean;
  message: string;
}

export interface EligibilityResult {
  eligible: boolean;
  checks: EligibilityCheck[];
}
