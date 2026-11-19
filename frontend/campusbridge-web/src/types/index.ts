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

// TODO: Define eligibility rule properties
export interface EligibilityRule {}

// TODO: Define job properties
export interface Job {}

export type ApplicationStatus = 'APPLIED' | 'SHORTLISTED' | 'SELECTED' | 'REJECTED';
