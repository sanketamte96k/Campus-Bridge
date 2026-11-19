import { canApply, canWithdraw } from '../features/applications/canApply';
import type { Application } from '../types';
import { getJobById } from './jobService';
import { getProfile } from './profileService';

// MOCK service. Later, replace with axios calls to POST /api/applications, GET /api/applications/me, PATCH /api/applications/{id}/withdraw.

const STORAGE_KEY = 'cb_applications';

const delay = (ms = 400): Promise<void> =>
  new Promise((resolve) => {
    setTimeout(resolve, ms);
  });

function getStoredApplications(): Application[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? (parsed as Application[]) : [];
  } catch {
    return [];
  }
}

function saveStoredApplications(apps: Application[]): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(apps));
}

export async function getMyApplications(userId: string): Promise<Application[]> {
  await delay(400);
  const all = getStoredApplications();
  return all.filter((app) => app.userId === userId);
}

export async function applyToJob(
  userId: string,
  jobId: string
): Promise<Application> {
  await delay(400);

  const job = await getJobById(jobId);
  if (!job) {
    throw new Error('That job no longer exists.');
  }

  const profile = await getProfile(userId);
  const all = getStoredApplications();
  const userApps = all.filter((app) => app.userId === userId);

  const decision = canApply({
    job,
    profile,
    existing: userApps,
    now: new Date(),
  });

  if (!decision.ok) {
    throw new Error(decision.reason);
  }

  const newApplication: Application = {
    id: crypto.randomUUID(),
    jobId: job.id,
    userId,
    status: 'APPLIED',
    appliedAt: new Date().toISOString(),
  };

  all.push(newApplication);
  saveStoredApplications(all);

  return newApplication;
}

export async function withdrawApplication(
  userId: string,
  applicationId: string
): Promise<Application> {
  await delay(400);

  const all = getStoredApplications();
  const index = all.findIndex(
    (app) => app.id === applicationId && app.userId === userId
  );

  if (index === -1) {
    throw new Error('Application not found.');
  }

  const application = all[index];
  if (!canWithdraw(application)) {
    throw new Error('Only submitted applications can be withdrawn.');
  }

  const updated: Application = {
    ...application,
    status: 'WITHDRAWN',
  };

  all[index] = updated;
  saveStoredApplications(all);

  return updated;
}
