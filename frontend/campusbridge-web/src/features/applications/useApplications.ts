import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../auth/AuthContext';
import {
  applyToJob,
  getMyApplications,
  withdrawApplication,
} from '../../services/applicationService';
import type { Application } from '../../types';

export function useMyApplications() {
  const { user } = useAuth();

  return useQuery<Application[]>({
    queryKey: ['applications'],
    queryFn: () => {
      if (!user) {
        return [];
      }
      return getMyApplications(user.id);
    },
    enabled: Boolean(user?.id),
  });
}

export function useApply() {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (jobId: string) => {
      if (!user) {
        throw new Error('User not authenticated.');
      }
      return applyToJob(user.id, jobId);
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['applications'] });
    },
  });
}

export function useWithdraw() {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (applicationId: string) => {
      if (!user) {
        throw new Error('User not authenticated.');
      }
      return withdrawApplication(user.id, applicationId);
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['applications'] });
    },
  });
}
