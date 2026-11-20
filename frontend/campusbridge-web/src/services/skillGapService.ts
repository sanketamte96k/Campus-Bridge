import apiClient from './apiClient';
import type { LearningPlan, SkillGap } from '../types';

export async function getSkillGap(jobId: string): Promise<SkillGap> {
  const res = await apiClient.get<SkillGap>(`/api/jobs/${encodeURIComponent(jobId)}/skill-gap`);
  return res.data;
}

export async function createLearningPlan(jobId: string): Promise<LearningPlan> {
  const res = await apiClient.post<LearningPlan>(`/api/jobs/${encodeURIComponent(jobId)}/learning-plan`);
  return res.data;
}
