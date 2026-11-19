import type { StudentProfile } from '../types';

// MOCK service. Later, replace with axios calls to GET and PUT /api/students/me/profile.

const delay = (ms = 300): Promise<void> =>
  new Promise((resolve) => {
    setTimeout(resolve, ms);
  });

const getStorageKey = (userId: string): string => `cb_profile_${userId}`;

export async function getProfile(userId: string): Promise<StudentProfile | null> {
  await delay(300);

  try {
    const raw = localStorage.getItem(getStorageKey(userId));
    if (!raw) {
      return null;
    }
    const parsed = JSON.parse(raw) as StudentProfile;
    if (
      parsed &&
      typeof parsed === 'object' &&
      typeof parsed.branch === 'string' &&
      typeof parsed.cgpa === 'number' &&
      typeof parsed.activeBacklogs === 'number' &&
      Array.isArray(parsed.skills)
    ) {
      return parsed;
    }
    return null;
  } catch {
    return null;
  }
}

export async function saveProfile(
  userId: string,
  profile: StudentProfile
): Promise<StudentProfile> {
  await delay(300);

  localStorage.setItem(getStorageKey(userId), JSON.stringify(profile));
  return profile;
}
