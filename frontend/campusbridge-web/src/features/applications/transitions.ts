import type { ApplicationStatus } from '../../types';

const TRANSITIONS: Record<ApplicationStatus, ApplicationStatus[]> = {
  APPLIED: ['SHORTLISTED', 'REJECTED'],
  SHORTLISTED: ['SELECTED', 'REJECTED'],
  SELECTED: [],
  REJECTED: [],
  WITHDRAWN: [],
};

export function allowedTransitions(status: ApplicationStatus): ApplicationStatus[] {
  return TRANSITIONS[status] ?? [];
}

export function canTransition(
  from: ApplicationStatus,
  to: ApplicationStatus
): boolean {
  return allowedTransitions(from).includes(to);
}
