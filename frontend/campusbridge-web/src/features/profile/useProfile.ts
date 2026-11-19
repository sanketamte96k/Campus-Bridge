import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../auth/AuthContext';
import { getProfile } from '../../services/profileService';
import type { StudentProfile } from '../../types';

export function useProfile() {
  const { user } = useAuth();

  const query = useQuery<StudentProfile | null>({
    queryKey: ['profile', user?.id],
    queryFn: () => {
      if (!user) {
        return null;
      }
      return getProfile(user.id);
    },
    enabled: Boolean(user?.id),
  });

  return {
    profile: query.data ?? null,
    isLoading: query.isLoading,
    isError: query.isError,
    refetch: query.refetch,
    query,
  };
}
