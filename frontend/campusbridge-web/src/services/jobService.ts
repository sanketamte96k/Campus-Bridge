import apiClient from './apiClient';
import type { Job, NewJobInput } from '../types';

export async function getJobs(): Promise<Job[]> {
  const res = await apiClient.get<Job[]>('/api/jobs');
  return res.data;
}

export async function getAllJobs(): Promise<Job[]> {
  return getJobs();
}

export async function searchJobs(query: string): Promise<Job[]> {
  const trimmed = query.trim();
  if (!trimmed) {
    return getJobs();
  }
  const res = await apiClient.get<Job[]>('/api/jobs', {
    params: { search: trimmed },
  });
  return res.data;
}

export async function getJobById(id: string): Promise<Job | undefined> {
  try {
    const res = await apiClient.get<Job>(`/api/jobs/${encodeURIComponent(id)}`);
    return res.data ?? undefined;
  } catch {
    return undefined;
  }
}

export async function createJob(
  _recruiterId: string,
  input: NewJobInput
): Promise<Job> {
  const res = await apiClient.post<Job>('/api/jobs', input);
  return res.data;
}

export async function getJobsByRecruiter(_recruiterId: string): Promise<Job[]> {
  const res = await apiClient.get<Job[]>('/api/recruiter/jobs');
  return res.data;
}
