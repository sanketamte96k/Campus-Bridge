import { canApply, canWithdraw } from '../features/applications/canApply';
import { canTransition } from '../features/applications/transitions';
import type { Applicant, Application, ApplicationStatus } from '../types';
import { getJobById, getJobsByRecruiter } from './jobService';
import { getProfile } from './profileService';

// MOCK service. Later, replace with axios calls to:
// POST /api/applications, GET /api/applications/me, PATCH /api/applications/{id}/withdraw,
// GET /api/jobs/{id}/applicants, PATCH /api/applications/{id}/status.

const STORAGE_KEY = 'cb_applications';
const USERS_STORAGE_KEY = 'cb_users';

const delay = (ms = 400): Promise<void> =>
  new Promise((resolve) => {
    setTimeout(resolve, ms);
  });

interface StoredUser {
  id: string;
  name: string;
  email: string;
}

function getStoredUsers(): StoredUser[] {
  try {
    const raw = localStorage.getItem(USERS_STORAGE_KEY);
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

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

export async function getApplicantsForJob(
  recruiterId: string,
  jobId: string
): Promise<Applicant[]> {
  await delay(400);

  const job = await getJobById(jobId);
  if (!job) {
    throw new Error('Job not found.');
  }

  if (job.postedBy !== recruiterId) {
    throw new Error("You can't view applicants for this job.");
  }

  const all = getStoredApplications();
  const jobApplications = all.filter((app) => app.jobId === jobId);

  const users = getStoredUsers();
  const userMap = new Map(users.map((u) => [u.id, u]));

  const applicants: Applicant[] = await Promise.all(
    jobApplications.map(async (app) => {
      const user = userMap.get(app.userId);
      const profile = await getProfile(app.userId);

      return {
        application: app,
        name: user?.name ?? 'Unknown applicant',
        email: user?.email ?? 'Unknown email',
        profile,
      };
    })
  );

  return applicants.sort(
    (a, b) =>
      new Date(b.application.appliedAt).getTime() -
      new Date(a.application.appliedAt).getTime()
  );
}

export async function updateApplicationStatus(
  recruiterId: string,
  applicationId: string,
  newStatus: ApplicationStatus
): Promise<Application> {
  await delay(400);

  const all = getStoredApplications();
  const index = all.findIndex((app) => app.id === applicationId);

  if (index === -1) {
    throw new Error('Application not found.');
  }

  const application = all[index];
  const job = await getJobById(application.jobId);
  if (!job) {
    throw new Error('Job not found.');
  }

  if (job.postedBy !== recruiterId) {
    throw new Error("You can't view applicants for this job.");
  }

  if (!canTransition(application.status, newStatus)) {
    throw new Error("That status change isn't allowed.");
  }

  const updated: Application = {
    ...application,
    status: newStatus,
  };

  all[index] = updated;
  saveStoredApplications(all);

  return updated;
}

export async function getJobApplicantCounts(
  jobIds: string[]
): Promise<Record<string, number>> {
  await delay(200);

  const all = getStoredApplications();
  const idSet = new Set(jobIds);
  const counts: Record<string, number> = {};

  for (const id of jobIds) {
    counts[id] = 0;
  }

  for (const app of all) {
    if (idSet.has(app.jobId) && app.status !== 'WITHDRAWN') {
      counts[app.jobId] = (counts[app.jobId] ?? 0) + 1;
    }
  }

  return counts;
}

export async function getRecruiterStats(
  recruiterId: string
): Promise<{ jobs: number; applicants: number; shortlisted: number }> {
  await delay(400);

  const recruiterJobs = await getJobsByRecruiter(recruiterId);
  const recruiterJobIds = new Set(recruiterJobs.map((j) => j.id));

  const allApplications = getStoredApplications();
  const relevantApps = allApplications.filter((app) =>
    recruiterJobIds.has(app.jobId)
  );

  const activeApps = relevantApps.filter((app) => app.status !== 'WITHDRAWN');
  const shortlistedApps = activeApps.filter(
    (app) => app.status === 'SHORTLISTED'
  );

  return {
    jobs: recruiterJobs.length,
    applicants: activeApps.length,
    shortlisted: shortlistedApps.length,
  };
}
