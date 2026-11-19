import { useQuery } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import type { ReactElement } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button } from '../../components/Button';
import { StatusBadge } from '../../components/StatusBadge';
import { allowedTransitions } from '../../features/applications/transitions';
import { evaluateEligibility } from '../../features/eligibility/evaluateEligibility';
import {
  useJobApplicants,
  useUpdateStatus,
} from '../../features/recruiter/useRecruiter';
import { getJobById } from '../../services/jobService';
import type {
  ApplicationStatus,
  EligibilityRule,
  EligibilityRuleKey,
  StudentProfile,
} from '../../types';

const ACTION_LABELS: Record<string, string> = {
  SHORTLISTED: 'Shortlist',
  SELECTED: 'Select',
  REJECTED: 'Reject',
};

function getEligibilitySummary(
  profile: StudentProfile | null,
  rules: EligibilityRule
): { text: string; meets: boolean } {
  if (!profile) {
    return { text: 'No profile', meets: false };
  }

  const result = evaluateEligibility(profile, rules);
  if (result.eligible) {
    return { text: 'Meets all criteria', meets: true };
  }

  const ruleNameMap: Record<EligibilityRuleKey, string> = {
    BRANCH: 'Branch',
    CGPA: 'CGPA',
    BACKLOGS: 'Backlogs',
    SKILLS: 'Skills',
  };

  const misses = result.checks
    .filter((c) => !c.passed)
    .map((c) => ruleNameMap[c.rule]);

  return {
    text: `Misses: ${misses.join(', ')}`,
    meets: false,
  };
}

export function Applicants(): ReactElement {
  const { jobId = '' } = useParams<{ jobId: string }>();

  const {
    data: job,
    isLoading: isJobLoading,
    error: jobError,
  } = useQuery({
    queryKey: ['job', jobId],
    queryFn: () => getJobById(jobId),
    enabled: Boolean(jobId),
  });

  const {
    data: applicants = [],
    isLoading: isAppsLoading,
    error: appsError,
    refetch: refetchApps,
  } = useJobApplicants(jobId);

  const updateStatusMutation = useUpdateStatus(jobId);
  const [updatingAppId, setUpdatingAppId] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const filteredApplicants = useMemo(() => {
    if (statusFilter === 'ALL') {
      return applicants;
    }
    return applicants.filter((app) => app.application.status === statusFilter);
  }, [applicants, statusFilter]);

  const handleStatusChange = (
    applicationId: string,
    newStatus: ApplicationStatus
  ): void => {
    setActionError(null);
    setUpdatingAppId(applicationId);

    updateStatusMutation.mutate(
      { applicationId, newStatus },
      {
        onError: (err) => {
          setActionError(
            err instanceof Error ? err.message : 'Could not update applicant status.'
          );
        },
        onSettled: () => {
          setUpdatingAppId(null);
        },
      }
    );
  };

  const errorMessage =
    (appsError instanceof Error ? appsError.message : null) ||
    (jobError instanceof Error ? jobError.message : null);

  const isLoading = isJobLoading || isAppsLoading;

  return (
    <div className="max-w-4xl">
      <div className="mb-6">
        <Link
          to="/recruiter/jobs"
          className="text-xs font-semibold text-brand hover:underline"
        >
          &larr; Back to your jobs
        </Link>
        <h1 className="mt-2 text-2xl font-bold tracking-tight text-ink sm:text-3xl">
          {job ? `Applicants for ${job.title}` : 'Job applicants'}
        </h1>
        <p className="mt-1 text-sm text-ink/70">
          Review candidate eligibility and advance applicants through recruitment stages.
        </p>
      </div>

      {actionError && (
        <div
          role="alert"
          className="mb-4 rounded-md border border-alert/20 bg-alert/10 p-3 text-sm text-alert"
        >
          {actionError}
        </div>
      )}

      {errorMessage && (
        <div
          role="alert"
          className="rounded-md border border-alert/20 bg-alert/10 p-4 text-sm text-alert"
        >
          {errorMessage}
        </div>
      )}

      {isLoading && (
        <div className="py-8 text-sm text-ink/70">Loading applicants...</div>
      )}

      {!errorMessage && !isLoading && (
        <>
          <div className="mb-6 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
            <span className="text-sm text-ink/70">
              Showing {filteredApplicants.length} of {applicants.length}{' '}
              {applicants.length === 1 ? 'applicant' : 'applicants'}
            </span>

            <div className="flex items-center gap-2">
              <label htmlFor="status-filter" className="text-xs font-medium text-ink">
                Status:
              </label>
              <select
                id="status-filter"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="rounded-md border border-line bg-white px-3 py-1.5 text-xs text-ink focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20"
              >
                <option value="ALL">All</option>
                <option value="APPLIED">Applied</option>
                <option value="SHORTLISTED">Shortlisted</option>
                <option value="SELECTED">Selected</option>
                <option value="REJECTED">Rejected</option>
                <option value="WITHDRAWN">Withdrawn</option>
              </select>
            </div>
          </div>

          {filteredApplicants.length === 0 ? (
            <div className="rounded-lg border border-line bg-white p-8 text-center sm:p-12">
              <h2 className="text-lg font-bold text-ink">No applicants yet</h2>
              <p className="mt-1 text-sm text-ink/70">
                Students who apply will show up here.
              </p>
              <div className="mt-4">
                <Button variant="secondary" onClick={() => void refetchApps()}>
                  Refresh list
                </Button>
              </div>
            </div>
          ) : (
            <div className="flex flex-col gap-4">
              {filteredApplicants.map(({ application, name, email, profile }) => {
                const formattedDate = new Date(
                  application.appliedAt
                ).toLocaleDateString('en-IN', {
                  day: 'numeric',
                  month: 'short',
                  year: 'numeric',
                });

                const summary = job
                  ? getEligibilitySummary(profile, job.rules)
                  : { text: 'Unknown', meets: false };

                const moves = allowedTransitions(application.status);

                return (
                  <article
                    key={application.id}
                    className="flex flex-col gap-4 rounded-lg border border-line bg-white p-5 shadow-xs"
                  >
                    <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-start">
                      <div>
                        <h2 className="text-base font-bold text-ink">{name}</h2>
                        <p className="text-xs text-ink/70">{email}</p>
                      </div>

                      <div className="flex items-center gap-3">
                        <span
                          className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${
                            summary.meets
                              ? 'border border-emerald-200 bg-emerald-50 text-emerald-700'
                              : 'border border-line bg-paper text-ink/70'
                          }`}
                        >
                          {summary.text}
                        </span>
                        <StatusBadge status={application.status} />
                      </div>
                    </div>

                    {profile ? (
                      <dl className="grid grid-cols-2 gap-3 border-y border-line py-3 text-xs sm:grid-cols-4">
                        <div>
                          <dt className="text-ink/60">Branch</dt>
                          <dd className="font-semibold text-ink">
                            {profile.branch}
                          </dd>
                        </div>
                        <div>
                          <dt className="text-ink/60">CGPA</dt>
                          <dd className="font-semibold text-ink">
                            {profile.cgpa}
                          </dd>
                        </div>
                        <div>
                          <dt className="text-ink/60">Active backlogs</dt>
                          <dd className="font-semibold text-ink">
                            {profile.activeBacklogs}
                          </dd>
                        </div>
                        <div>
                          <dt className="text-ink/60">Applied on</dt>
                          <dd className="font-semibold text-ink">
                            {formattedDate}
                          </dd>
                        </div>
                      </dl>
                    ) : (
                      <p className="border-y border-line py-2 text-xs italic text-ink/60">
                        Academic profile has not been completed.
                      </p>
                    )}

                    {profile && profile.skills.length > 0 && (
                      <div className="flex flex-wrap gap-1.5">
                        {profile.skills.map((skill) => (
                          <span
                            key={skill}
                            className="rounded-full bg-paper px-2.5 py-0.5 text-xs font-medium text-ink/80"
                          >
                            {skill}
                          </span>
                        ))}
                      </div>
                    )}

                    {moves.length > 0 && (
                      <div className="flex flex-wrap items-center gap-2 border-t border-line pt-3">
                        {moves.map((targetStatus) => {
                          const isReject = targetStatus === 'REJECTED';
                          const isBusy = updatingAppId === application.id;

                          return (
                            <Button
                              key={targetStatus}
                              variant={isReject ? 'secondary' : 'primary'}
                              onClick={() =>
                                handleStatusChange(
                                  application.id,
                                  targetStatus
                                )
                              }
                              disabled={isBusy}
                              className="text-xs"
                            >
                              {ACTION_LABELS[targetStatus] ?? targetStatus}
                            </Button>
                          );
                        })}
                      </div>
                    )}
                  </article>
                );
              })}
            </div>
          )}
        </>
      )}
    </div>
  );
}
