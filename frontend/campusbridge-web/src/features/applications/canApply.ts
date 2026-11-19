import { evaluateEligibility } from '../eligibility/evaluateEligibility';
import type { Application, Job, StudentProfile } from '../../types';

export interface CanApplyInput {
  job: Job;
  profile: StudentProfile | null;
  existing: Application[];
  now: Date;
}

export type CanApplyResult = { ok: true } | { ok: false; reason: string };

export function canApply(input: CanApplyInput): CanApplyResult {
  // 1. Profile must exist
  if (!input.profile) {
    return { ok: false, reason: 'Complete your profile before applying.' };
  }

  // 2. Deadline check: open through the end of the deadline date in local time
  const deadlineEnd = new Date(`${input.job.deadline}T23:59:59`);
  if (input.now.getTime() > deadlineEnd.getTime()) {
    return { ok: false, reason: 'Applications for this job are closed.' };
  }

  // 3. Existing active application check (status not WITHDRAWN)
  const existingActive = input.existing.find(
    (app) => app.jobId === input.job.id && app.status !== 'WITHDRAWN'
  );
  if (existingActive) {
    return { ok: false, reason: "You've already applied to this job." };
  }

  // 4. Eligibility check
  const eligibility = evaluateEligibility(input.profile, input.job.rules);
  if (!eligibility.eligible) {
    return { ok: false, reason: "You're not eligible for this job." };
  }

  return { ok: true };
}

export function canWithdraw(application: Application): boolean {
  return application.status === 'APPLIED';
}
