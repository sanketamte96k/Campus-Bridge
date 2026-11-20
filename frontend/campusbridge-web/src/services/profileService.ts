import apiClient from './apiClient';
import type { StudentProfile } from '../types';

export async function getProfile(userId: string): Promise<StudentProfile | null> {
  try {
    const res = await apiClient.get<StudentProfile | null>(
      `/api/students/${encodeURIComponent(userId)}/profile`
    );
    if (!res.data || !res.data.branch) {
      return null;
    }
    return res.data;
  } catch {
    return null;
  }
}

export async function saveProfile(
  _userId: string,
  profile: StudentProfile
): Promise<StudentProfile> {
  const res = await apiClient.put<StudentProfile>('/api/students/profile', profile);
  return res.data;
}
