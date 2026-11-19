import { useQuery } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import type { ReactElement } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../../components/Button';
import { StatusBadge } from '../../components/StatusBadge';
import { canWithdraw } from '../../features/applications/canApply';
import {
  useMyApplications,
  useWithdraw,
} from '../../features/applications/useApplications';
import { getJobs } from '../../services/jobService';

export function Applications(): ReactElement {
  const {
    data: applications = [],
    isLoading: isAppsLoading,
    isError: isAppsError,
    refetch: refetchApps,
  } = useMyApplications();

  const {
    data: jobs = [],
    isLoading: isJobsLoading,
    isError: isJobsError,
    refetch: refetchJobs,
  } = useQuery({
    queryKey: ['jobs'],
    queryFn: getJobs,
  });

  const withdrawMutation = useWithdraw();
  const [withdrawingId, setWithdrawingId] = useState<string | null>(null);
  const [withdrawError, setWithdrawError] = useState<string | null>(null);

  const handleWithdraw = (applicationId: string): void => {
    setWithdrawError(null);
    setWithdrawingId(applicationId);

    withdrawMutation.mutate(applicationId, {
      onError: (err) => {
        setWithdrawError(
          err instanceof Error ? err.message : 'Could not withdraw application.'
        );
      },
      onSettled: () => {
        setWithdrawingId(null);
      },
    });
  };

  const handleRetry = (): void => {
    void refetchApps();
    void refetchJobs();
  };

  // Join applications with jobs, sorted newest first
  const sortedApplications = useMemo(() => {
    const jobMap = new Map(jobs.map((job) => [job.id, job]));

    return [...applications]
      .sort(
        (a, b) =>
          new Date(b.appliedAt).getTime() - new Date(a.appliedAt).getTime()
      )
      .map((app) => ({
        application: app,
        job: jobMap.get(app.jobId),
      }));
  }, [applications, jobs]);

  const isLoading = isAppsLoading || isJobsLoading;
  const isError = isAppsError || isJobsError;

  return (
    <div className="max-w-4xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">
          Your applications
        </h1>
        <p className="mt-1 text-sm text-ink/70">
          Track every job you've applied to.
        </p>
      </div>

      {withdrawError && (
        <div
          role="alert"
          className="mb-4 rounded-md border border-alert/20 bg-alert/10 p-3 text-sm text-alert"
        >
          {withdrawError}
        </div>
      )}

      {isLoading && (
        <div className="py-8 text-sm text-ink/70">Loading applications...</div>
      )}

      {isError && (
        <div
          role="alert"
          className="flex items-center justify-between rounded-md border border-alert/20 bg-alert/10 p-4 text-sm text-alert"
        >
          <span>Couldn't load applications.</span>
          <Button variant="secondary" onClick={handleRetry}>
            Try again
          </Button>
        </div>
      )}

      {!isLoading && !isError && sortedApplications.length === 0 && (
        <div className="rounded-lg border border-line bg-white p-8 text-center sm:p-12">
          <h2 className="text-lg font-bold text-ink">No applications yet</h2>
          <p className="mt-1 text-sm text-ink/70">
            Jobs you apply to will show up here.
          </p>
          <div className="mt-6">
            <Link
              to="/student/jobs"
              className="inline-flex items-center justify-center rounded-md bg-brand px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-brand-dark focus:outline-none focus:ring-2 focus:ring-brand/40"
            >
              Browse jobs
            </Link>
          </div>
        </div>
      )}

      {!isLoading && !isError && sortedApplications.length > 0 && (
        <div className="flex flex-col gap-4">
          {sortedApplications.map(({ application, job }) => {
            const formattedDate = new Date(
              application.appliedAt
            ).toLocaleDateString('en-IN', {
              day: 'numeric',
              month: 'short',
              year: 'numeric',
            });

            return (
              <article
                key={application.id}
                className="flex flex-col justify-between gap-4 rounded-lg border border-line bg-white p-5 sm:flex-row sm:items-center"
              >
                <div>
                  <h2 className="text-lg font-bold text-ink">
                    {job?.title ?? 'Role unavailable'}
                  </h2>
                  <p className="text-sm text-ink/70">
                    {job ? `${job.company}, ${job.location}` : 'Company details unavailable'}
                  </p>
                  <p className="mt-2 text-xs text-ink/60">
                    Applied on {formattedDate}
                  </p>
                </div>

                <div className="flex items-center gap-3 self-start sm:self-center">
                  <StatusBadge status={application.status} />

                  {canWithdraw(application) && (
                    <Button
                      variant="secondary"
                      onClick={() => handleWithdraw(application.id)}
                      isLoading={withdrawingId === application.id}
                      loadingText="Withdrawing..."
                      disabled={withdrawingId === application.id}
                      className="text-xs"
                    >
                      Withdraw
                    </Button>
                  )}
                </div>
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}
