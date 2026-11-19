import type { Job, NewJobInput } from '../types';

// MOCK service. Later, replace with axios calls (GET /api/jobs, GET /api/jobs/{id}, POST /api/jobs, GET /api/jobs/mine).

const JOBS_STORAGE_KEY = 'cb_jobs';

const delay = (ms = 400): Promise<void> =>
  new Promise((resolve) => {
    setTimeout(resolve, ms);
  });

const SEED_JOBS: Job[] = [
  {
    id: 'job-1',
    title: 'Java developer',
    company: 'Northwind Technologies',
    location: 'Pune',
    packageLpa: 6,
    deadline: '2026-12-15',
    description: 'Build robust enterprise backend services and scalable RESTful APIs with clean architecture.',
    rules: {
      allowedBranches: ['IT', 'CS'],
      minCgpa: 7.5,
      maxBacklogs: 0,
      requiredSkills: ['Java', 'SQL', 'Spring Boot'],
    },
    postedBy: 'seed',
  },
  {
    id: 'job-2',
    title: 'Frontend engineer',
    company: 'Pixelforge Labs',
    location: 'Bengaluru',
    packageLpa: 8,
    deadline: '2026-12-20',
    description: 'Craft responsive, user-friendly web interfaces and component systems with modern web standards.',
    rules: {
      allowedBranches: ['IT', 'CS', 'ENTC'],
      minCgpa: 7,
      maxBacklogs: 0,
      requiredSkills: ['React', 'TypeScript', 'Tailwind'],
    },
    postedBy: 'seed',
  },
  {
    id: 'job-3',
    title: 'Software trainee',
    company: 'Brightline Systems',
    location: 'Mumbai',
    packageLpa: 4.5,
    deadline: '2027-01-05',
    description: 'Kickstart your engineering career through practical software engineering training and team mentorship.',
    rules: {
      allowedBranches: ['IT', 'CS', 'ENTC', 'MECH'],
      minCgpa: 6,
      maxBacklogs: 1,
      requiredSkills: ['Java', 'DSA'],
    },
    postedBy: 'seed',
  },
  {
    id: 'job-4',
    title: 'Data analyst',
    company: 'Quantive Analytics',
    location: 'Hyderabad',
    packageLpa: 7,
    deadline: '2027-01-10',
    description: 'Transform complex institutional datasets into actionable insights and automated reporting dashboards.',
    rules: {
      allowedBranches: ['IT', 'CS'],
      minCgpa: 7.5,
      maxBacklogs: 0,
      requiredSkills: ['Python', 'SQL'],
    },
    postedBy: 'seed',
  },
  {
    id: 'job-5',
    title: 'Backend engineer',
    company: 'Cloudmesh',
    location: 'Pune',
    packageLpa: 10,
    deadline: '2027-01-20',
    description: 'Architect distributed backend systems and deploy containerized microservices to cloud infrastructure.',
    rules: {
      allowedBranches: ['IT', 'CS'],
      minCgpa: 8,
      maxBacklogs: 0,
      requiredSkills: ['Java', 'Spring Boot', 'PostgreSQL', 'Docker'],
    },
    postedBy: 'seed',
  },
];

function getStoredJobs(): Job[] {
  try {
    const raw = localStorage.getItem(JOBS_STORAGE_KEY);
    if (!raw) {
      localStorage.setItem(JOBS_STORAGE_KEY, JSON.stringify(SEED_JOBS));
      return SEED_JOBS;
    }
    const parsed = JSON.parse(raw);
    if (Array.isArray(parsed) && parsed.length > 0) {
      return parsed as Job[];
    }
    localStorage.setItem(JOBS_STORAGE_KEY, JSON.stringify(SEED_JOBS));
    return SEED_JOBS;
  } catch {
    return SEED_JOBS;
  }
}

function saveStoredJobs(jobs: Job[]): void {
  localStorage.setItem(JOBS_STORAGE_KEY, JSON.stringify(jobs));
}

export async function getJobs(): Promise<Job[]> {
  await delay(400);
  return getStoredJobs();
}

export async function getJobById(id: string): Promise<Job | undefined> {
  await delay(200);
  const jobs = getStoredJobs();
  return jobs.find((job) => job.id === id);
}

export async function createJob(
  recruiterId: string,
  input: NewJobInput
): Promise<Job> {
  await delay(400);

  // Validate deadline: must be today or later in local date
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const deadlineDate = new Date(`${input.deadline}T00:00:00`);
  if (deadlineDate.getTime() < today.getTime()) {
    throw new Error('Deadline must be today or later.');
  }

  // Validate allowed branches: at least one
  if (!input.rules.allowedBranches || input.rules.allowedBranches.length === 0) {
    throw new Error('Add at least one allowed branch.');
  }

  const jobs = getStoredJobs();
  const newJob: Job = {
    ...input,
    id: crypto.randomUUID(),
    postedBy: recruiterId,
  };

  jobs.push(newJob);
  saveStoredJobs(jobs);

  return newJob;
}

export async function getJobsByRecruiter(recruiterId: string): Promise<Job[]> {
  await delay(400);
  const jobs = getStoredJobs();
  return jobs.filter((job) => job.postedBy === recruiterId);
}
