import apiClient from './apiClient';
import type { Applicant, Application, ApplicationStatus } from '../types';

export async function getMyApplications(_userId: string): Promise<Application[]> {
  const res = await apiClient.get<Application[]>('/api/applications/my');
  return res.data;
}

export async function applyToJob(
  _userId: string,
  jobId: string
): Promise<Application> {
  const res = await apiClient.post<Application>('/api/applications', { jobId });
  return res.data;
}

export async function withdrawApplication(
  _userId: string,
  applicationId: string
): Promise<Application> {
  const res = await apiClient.post<Application>(
    `/api/applications/${encodeURIComponent(applicationId)}/withdraw`
  );
  return res.data;
}

export async function getApplicantsForJob(
  _recruiterId: string,
  jobId: string
): Promise<Applicant[]> {
  const res = await apiClient.get<Applicant[]>(
    `/api/recruiter/jobs/${encodeURIComponent(jobId)}/applicants`
  );
  return res.data;
}

export async function updateApplicationStatus(
  _recruiterId: string,
  applicationId: string,
  newStatus: ApplicationStatus
): Promise<Application> {
  const res = await apiClient.patch<Application>(
    `/api/applications/${encodeURIComponent(applicationId)}/status`,
    { status: newStatus }
  );
  return res.data;
}

export async function getJobApplicantCounts(
  jobIds: string[]
): Promise<Record<string, number>> {
  const res = await apiClient.get<Record<string, number>>(
    '/api/recruiter/jobs/applicant-counts'
  );
  const counts = res.data ?? {};
  const result: Record<string, number> = {};

  for (const id of jobIds) {
    result[id] = counts[id] ?? 0;
  }

  return result;
}

export async function getRecruiterStats(
  _recruiterId: string
): Promise<{ jobs: number; applicants: number; shortlisted: number }> {
  const res = await apiClient.get<{
    totalJobs: number;
    totalApplications: number;
    shortlistedCount: number;
    selectedCount: number;
    rejectedCount: number;
    pendingReviewCount: number;
  }>('/api/recruiter/stats');

  return {
    jobs: res.data.totalJobs,
    applicants: res.data.totalApplications,
    shortlisted: res.data.shortlistedCount,
  };
}
