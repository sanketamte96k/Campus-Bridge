import { describe, expect, it } from 'vitest';
import type { ApplicationStatus } from '../../types';
import { allowedTransitions, canTransition } from './transitions';

describe('application transitions', () => {
  const allStatuses: ApplicationStatus[] = [
    'APPLIED',
    'SHORTLISTED',
    'SELECTED',
    'REJECTED',
    'WITHDRAWN',
  ];

  const allowedPairs: [ApplicationStatus, ApplicationStatus][] = [
    ['APPLIED', 'SHORTLISTED'],
    ['APPLIED', 'REJECTED'],
    ['SHORTLISTED', 'SELECTED'],
    ['SHORTLISTED', 'REJECTED'],
  ];

  it('returns true for every allowed move', () => {
    for (const [from, to] of allowedPairs) {
      expect(canTransition(from, to)).toBe(true);
    }
  });

  it('returns false for every other pair of statuses across all 5x5 combinations', () => {
    const isAllowed = (from: ApplicationStatus, to: ApplicationStatus): boolean =>
      allowedPairs.some(([f, t]) => f === from && t === to);

    for (const from of allStatuses) {
      for (const to of allStatuses) {
        if (!isAllowed(from, to)) {
          expect(canTransition(from, to)).toBe(false);
        }
      }
    }
  });

  it('returns an empty allowedTransitions array for terminal states', () => {
    expect(allowedTransitions('SELECTED')).toEqual([]);
    expect(allowedTransitions('REJECTED')).toEqual([]);
    expect(allowedTransitions('WITHDRAWN')).toEqual([]);
  });

  it('returns valid transition lists for non-terminal states', () => {
    expect(allowedTransitions('APPLIED')).toEqual(['SHORTLISTED', 'REJECTED']);
    expect(allowedTransitions('SHORTLISTED')).toEqual(['SELECTED', 'REJECTED']);
  });
});
