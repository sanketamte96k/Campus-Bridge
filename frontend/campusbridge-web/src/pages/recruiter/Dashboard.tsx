import type { ReactElement } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../features/auth/AuthContext';
import { useRecruiterStats } from '../../features/recruiter/useRecruiter';

export function RecruiterDashboard(): ReactElement {
  const { user } = useAuth();
  const { data: stats, isLoading } = useRecruiterStats();

  const jobsCount = stats?.jobs ?? 0;
  const applicantsCount = stats?.applicants ?? 0;
  const shortlistedCount = stats?.shortlisted ?? 0;

  return (
    <div className="max-w-4xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">
          Welcome, {user?.name}
        </h1>
        <p className="mt-1 text-sm text-ink/70">
          Manage your company placement drives, review shortlisted applicants, and schedule interview rounds.
        </p>
      </div>

      {isLoading ? (
        <div className="py-8 text-sm text-ink/70">Loading stats...</div>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div className="rounded-lg border border-line bg-white p-5 shadow-xs">
            <p className="text-xs font-semibold uppercase tracking-wider text-ink/60">
              Jobs posted
            </p>
            <p className="mt-2 text-3xl font-bold text-ink">{jobsCount}</p>
          </div>

          <div className="rounded-lg border border-line bg-white p-5 shadow-xs">
            <p className="text-xs font-semibold uppercase tracking-wider text-ink/60">
              Applicants
            </p>
            <p className="mt-2 text-3xl font-bold text-ink">{applicantsCount}</p>
          </div>

          <div className="rounded-lg border border-line bg-white p-5 shadow-xs">
            <p className="text-xs font-semibold uppercase tracking-wider text-ink/60">
              Shortlisted
            </p>
            <p className="mt-2 text-3xl font-bold text-ink">{shortlistedCount}</p>
          </div>
        </div>
      )}

      {!isLoading && jobsCount === 0 && (
        <div className="mt-8 rounded-lg border border-line bg-white p-8 text-center sm:p-12">
          <h2 className="text-lg font-bold text-ink">Ready to hire?</h2>
          <p className="mt-1 text-sm text-ink/70">
            Post your first job drive to start receiving student applications.
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
    </div>
  );
}
