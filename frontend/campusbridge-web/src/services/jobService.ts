import type { Job } from '../types';

// MOCK service. Later, replace with axios calls to GET /api/jobs and GET /api/jobs/{id}.

const delay = (ms = 400): Promise<void> =>
  new Promise((resolve) => {
    setTimeout(resolve, ms);
  });

const MOCK_JOBS: Job[] = [
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
  },
];

export async function getJobs(): Promise<Job[]> {
  await delay(400);
  return MOCK_JOBS;
}

export async function getJobById(id: string): Promise<Job | undefined> {
  await delay(200);
  return MOCK_JOBS.find((job) => job.id === id);
}
