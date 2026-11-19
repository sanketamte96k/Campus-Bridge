import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../auth/AuthContext';
import {
  getApplicantsForJob,
  getJobApplicantCounts,
  getRecruiterStats,
  updateApplicationStatus,
} from '../../services/applicationService';
import { createJob, getJobsByRecruiter } from '../../services/jobService';
import type { Applicant, ApplicationStatus, Job, NewJobInput } from '../../types';

export function useRecruiterJobs() {
  const { user } = useAuth();

  return useQuery<Job[]>({
    queryKey: ['recruiter-jobs'],
    queryFn: () => {
      if (!user) {
        return [];
      }
      return getJobsByRecruiter(user.id);
    },
    enabled: Boolean(user?.id),
  });
}

export function useCreateJob() {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: NewJobInput) => {
      if (!user) {
        throw new Error('User not authenticated.');
      }
      return createJob(user.id, input);
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['recruiter-jobs'] });
      void queryClient.invalidateQueries({ queryKey: ['jobs'] });
      void queryClient.invalidateQueries({ queryKey: ['recruiter-stats'] });
    },
  });
}

export function useJobApplicants(jobId: string) {
  const { user } = useAuth();

  return useQuery<Applicant[]>({
    queryKey: ['job-applicants', jobId],
    queryFn: () => {
      if (!user) {
        return [];
      }
      return getApplicantsForJob(user.id, jobId);
    },
    enabled: Boolean(user?.id && jobId),
  });
}

export function useUpdateStatus(jobId: string) {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      applicationId,
      newStatus,
    }: {
      applicationId: string;
      newStatus: ApplicationStatus;
    }) => {
      if (!user) {
        throw new Error('User not authenticated.');
      }
      return updateApplicationStatus(user.id, applicationId, newStatus);
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: ['job-applicants', jobId],
      });
      void queryClient.invalidateQueries({ queryKey: ['recruiter-stats'] });
      void queryClient.invalidateQueries({ queryKey: ['applications'] });
    },
  });
}

export function useJobApplicantCounts(jobIds: string[]) {
  return useQuery<Record<string, number>>({
    queryKey: ['job-applicant-counts', jobIds],
    queryFn: () => getJobApplicantCounts(jobIds),
    enabled: jobIds.length > 0,
  });
}

export function useRecruiterStats() {
  const { user } = useAuth();

  return useQuery<{ jobs: number; applicants: number; shortlisted: number }>({
    queryKey: ['recruiter-stats'],
    queryFn: () => {
      if (!user) {
        return { jobs: 0, applicants: 0, shortlisted: 0 };
      }
      return getRecruiterStats(user.id);
    },
    enabled: Boolean(user?.id),
  });
}
