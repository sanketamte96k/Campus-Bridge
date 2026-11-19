import { describe, expect, it } from 'vitest';
import type { Application, Job, StudentProfile } from '../../types';
import { canApply, canWithdraw } from './canApply';

describe('canApply', () => {
  const eligibleProfile: StudentProfile = {
    branch: 'IT',
    cgpa: 8.5,
    activeBacklogs: 0,
    skills: ['Java', 'SQL', 'Spring Boot'],
  };

  const sampleJob: Job = {
    id: 'job-1',
    title: 'Java developer',
    company: 'Northwind Technologies',
    location: 'Pune',
    packageLpa: 6,
    deadline: '2026-12-15',
    description: 'Build enterprise backend services.',
    rules: {
      allowedBranches: ['IT', 'CS'],
      minCgpa: 7.5,
      maxBacklogs: 0,
      requiredSkills: ['Java', 'SQL', 'Spring Boot'],
    },
  };

  const testNow = new Date('2026-12-01T10:00:00');

  it('returns ok: true when all conditions are satisfied', () => {
    const result = canApply({
      job: sampleJob,
      profile: eligibleProfile,
      existing: [],
      now: testNow,
    });

    expect(result).toEqual({ ok: true });
  });

  it('fails when profile is null', () => {
    const result = canApply({
      job: sampleJob,
      profile: null,
      existing: [],
      now: testNow,
    });

    expect(result).toEqual({
      ok: false,
      reason: 'Complete your profile before applying.',
    });
  });

  it('fails when deadline has passed', () => {
    const afterDeadline = new Date('2026-12-16T00:00:01');
    const result = canApply({
      job: sampleJob,
      profile: eligibleProfile,
      existing: [],
      now: afterDeadline,
    });

    expect(result).toEqual({
      ok: false,
      reason: 'Applications for this job are closed.',
    });
  });

  it('allows applying on the deadline day itself', () => {
    const deadlineDay = new Date('2026-12-15T20:30:00');
    const result = canApply({
      job: sampleJob,
      profile: eligibleProfile,
      existing: [],
      now: deadlineDay,
    });

    expect(result).toEqual({ ok: true });
  });

  it('fails when an active (non-withdrawn) application already exists', () => {
    const existingApp: Application = {
      id: 'app-1',
      jobId: sampleJob.id,
      userId: 'user-1',
      status: 'APPLIED',
      appliedAt: '2026-12-01T08:00:00Z',
    };

    const result = canApply({
      job: sampleJob,
      profile: eligibleProfile,
      existing: [existingApp],
      now: testNow,
    });

    expect(result).toEqual({
      ok: false,
      reason: "You've already applied to this job.",
    });
  });

  it('allows re-applying if the existing application was WITHDRAWN', () => {
    const withdrawnApp: Application = {
      id: 'app-1',
      jobId: sampleJob.id,
      userId: 'user-1',
      status: 'WITHDRAWN',
      appliedAt: '2026-12-01T08:00:00Z',
    };

    const result = canApply({
      job: sampleJob,
      profile: eligibleProfile,
      existing: [withdrawnApp],
      now: testNow,
    });

    expect(result).toEqual({ ok: true });
  });

  it('fails when student is not eligible for the job', () => {
    const ineligibleProfile: StudentProfile = {
      ...eligibleProfile,
      cgpa: 6.5, // Requires 7.5
    };

    const result = canApply({
      job: sampleJob,
      profile: ineligibleProfile,
      existing: [],
      now: testNow,
    });

    expect(result).toEqual({
      ok: false,
      reason: "You're not eligible for this job.",
    });
  });

  it('enforces check order: closed job reports closed even if student is also ineligible', () => {
    const ineligibleProfile: StudentProfile = {
      ...eligibleProfile,
      cgpa: 6.0,
      branch: 'CIVIL',
    };
    const afterDeadline = new Date('2026-12-16T12:00:00');

    const result = canApply({
      job: sampleJob,
      profile: ineligibleProfile,
      existing: [],
      now: afterDeadline,
    });

    expect(result).toEqual({
      ok: false,
      reason: 'Applications for this job are closed.',
    });
  });
});

describe('canWithdraw', () => {
  const baseApp: Application = {
    id: 'app-1',
    jobId: 'job-1',
    userId: 'user-1',
    status: 'APPLIED',
    appliedAt: '2026-12-01T08:00:00Z',
  };

  it('returns true when status is APPLIED', () => {
    expect(canWithdraw(baseApp)).toBe(true);
  });

  it('returns false for SHORTLISTED, SELECTED, REJECTED, and WITHDRAWN', () => {
    expect(canWithdraw({ ...baseApp, status: 'SHORTLISTED' })).toBe(false);
    expect(canWithdraw({ ...baseApp, status: 'SELECTED' })).toBe(false);
    expect(canWithdraw({ ...baseApp, status: 'REJECTED' })).toBe(false);
    expect(canWithdraw({ ...baseApp, status: 'WITHDRAWN' })).toBe(false);
  });
});
