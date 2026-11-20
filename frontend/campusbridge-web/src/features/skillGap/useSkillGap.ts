import { useMutation, useQuery } from '@tanstack/react-query';
import { createLearningPlan, getSkillGap } from '../../services/skillGapService';
import type { LearningPlan, SkillGap } from '../../types';

export function useSkillGap(jobId: string, enabled: boolean) {
  return useQuery<SkillGap>({
    queryKey: ['skillGap', jobId],
    queryFn: () => getSkillGap(jobId),
    enabled: Boolean(jobId) && enabled,
    staleTime: 1000 * 60 * 5,
  });
}

export function useLearningPlan(jobId: string) {
  return useMutation<LearningPlan, unknown>({
    mutationFn: () => createLearningPlan(jobId),
  });
}
