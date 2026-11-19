import type { ReactElement } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../../components/Button';
import {
  useJobApplicantCounts,
  useRecruiterJobs,
} from '../../features/recruiter/useRecruiter';

export function MyJobs(): ReactElement {
  const { data: jobs = [], isLoading, isError, refetch } = useRecruiterJobs();
  const jobIds = jobs.map((j) => j.id);
  const { data: counts = {} } = useJobApplicantCounts(jobIds);

  return (
    <div className="max-w-4xl">
      <div className="mb-6 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">
            Your jobs
          </h1>
          <p className="mt-1 text-sm text-ink/70">
            View active placement drives and manage candidate applications.
          </p>
        </div>

        <div>
          <Link
            to="/recruiter/jobs/new"
            className="inline-flex items-center justify-center rounded-md bg-brand px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-brand-dark focus:outline-none focus:ring-2 focus:ring-brand/40"
          >
            Post a job
          </Link>
        </div>
      </div>

      {isLoading && (
        <div className="py-8 text-sm text-ink/70">Loading jobs...</div>
      )}

      {isError && (
        <div
          role="alert"
          className="flex items-center justify-between rounded-md border border-alert/20 bg-alert/10 p-4 text-sm text-alert"
        >
          <span>Couldn't load jobs.</span>
          <Button variant="secondary" onClick={() => void refetch()}>
            Try again
          </Button>
        </div>
      )}

      {!isLoading && !isError && jobs.length === 0 && (
        <div className="rounded-lg border border-line bg-white p-8 text-center sm:p-12">
          <h2 className="text-lg font-bold text-ink">No jobs yet</h2>
          <p className="mt-1 text-sm text-ink/70">
            Post a job to start receiving applications.
          </p>
          <div className="mt-6">
            <Link
              to="/recruiter/jobs/new"
              className="inline-flex items-center justify-center rounded-md bg-brand px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-brand-dark focus:outline-none focus:ring-2 focus:ring-brand/40"
            >
              Post a job
            </Link>
          </div>
        </div>
      )}

      {!isLoading && !isError && jobs.length > 0 && (
        <div className="flex flex-col gap-4">
          {jobs.map((job) => {
            const formattedDeadline = new Date(
              `${job.deadline}T00:00:00`
            ).toLocaleDateString('en-IN', {
              day: 'numeric',
              month: 'short',
              year: 'numeric',
            });

            const applicantCount = counts[job.id] ?? 0;

            return (
              <article
                key={job.id}
                className="flex flex-col justify-between gap-4 rounded-lg border border-line bg-white p-5 sm:flex-row sm:items-center"
              >
                <div>
                  <h2 className="text-lg font-bold text-ink">{job.title}</h2>
                  <p className="text-sm text-ink/70">
                    {job.company}, {job.location}
                  </p>
                  <div className="mt-2 flex flex-wrap items-center gap-3 text-xs text-ink/60">
                    <span>Apply by {formattedDeadline}</span>
                    <span>&bull;</span>
                    <span className="font-medium text-ink">
                      {applicantCount} {applicantCount === 1 ? 'applicant' : 'applicants'}
                    </span>
                  </div>
                </div>

                <div className="shrink-0 self-start sm:self-center">
                  <Link
                    to={`/recruiter/jobs/${job.id}/applicants`}
                    className="inline-flex items-center justify-center rounded-md border border-line bg-white px-3.5 py-1.5 text-xs font-medium text-ink transition-colors hover:bg-paper focus:outline-none focus:ring-2 focus:ring-brand/20"
                  >
                    View applicants
                  </Link>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}
