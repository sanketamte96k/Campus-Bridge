import { useQuery } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import type { ChangeEvent, ReactElement } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../../components/Button';
import { JobCard } from '../../components/JobCard';
import { evaluateEligibility } from '../../features/eligibility/evaluateEligibility';
import { useProfile } from '../../features/profile/useProfile';
import { getJobs } from '../../services/jobService';

export function Jobs(): ReactElement {
  const [searchTerm, setSearchTerm] = useState('');
  const [eligibleOnly, setEligibleOnly] = useState(false);
  const { profile, isLoading: isProfileLoading } = useProfile();

  const {
    data: jobs = [],
    isLoading: isJobsLoading,
    isError,
    refetch,
  } = useQuery({
    queryKey: ['jobs'],
    queryFn: getJobs,
  });

  const handleSearchChange = (event: ChangeEvent<HTMLInputElement>): void => {
    setSearchTerm(event.target.value);
  };

  const filteredJobs = useMemo(() => {
    const query = searchTerm.trim().toLowerCase();

    return jobs.filter((job) => {
      if (query) {
        const matchesTitle = job.title.toLowerCase().includes(query);
        const matchesCompany = job.company.toLowerCase().includes(query);
        const matchesSkill = job.rules.requiredSkills.some((skill) =>
          skill.toLowerCase().includes(query)
        );
        if (!matchesTitle && !matchesCompany && !matchesSkill) {
          return false;
        }
      }

      if (eligibleOnly && profile) {
        const result = evaluateEligibility(profile, job.rules);
        if (!result.eligible) {
          return false;
        }
      }

      return true;
    });
  }, [jobs, searchTerm, eligibleOnly, profile]);

  return (
    <div className="max-w-4xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">Jobs</h1>
        <p className="mt-1 text-sm text-ink/70">
          Open roles from companies visiting your campus.
        </p>
      </div>

      {!isProfileLoading && !profile && (
        <div className="mb-6 flex items-center justify-between rounded-lg border border-brand/20 bg-brand/5 p-4 text-sm text-ink">
          <span>Complete your profile to see which jobs you're eligible for.</span>
          <Link
            to="/student/profile"
            className="ml-4 shrink-0 font-semibold text-brand hover:underline"
          >
            Complete profile
          </Link>
        </div>
      )}

      <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex-1">
          <input
            type="search"
            aria-label="Search jobs"
            placeholder="Search by title, company or skill"
            value={searchTerm}
            onChange={handleSearchChange}
            className="w-full rounded-md border border-line bg-white px-3.5 py-2.5 text-sm text-ink placeholder:text-gray-400 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20 transition-colors"
          />
        </div>

        {profile && (
          <label className="flex cursor-pointer items-center gap-2 select-none text-sm font-medium text-ink">
            <input
              type="checkbox"
              checked={eligibleOnly}
              onChange={(e) => setEligibleOnly(e.target.checked)}
              className="h-4 w-4 rounded border-line text-brand focus:ring-brand"
            />
            <span>Show only jobs I'm eligible for</span>
          </label>
        )}
      </div>

      {isJobsLoading && (
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

      {!isJobsLoading && !isError && filteredJobs.length === 0 && (
        <div className="rounded-lg border border-line bg-white p-8 text-center text-sm text-ink/70">
          No jobs match your search. Try a different skill or company.
        </div>
      )}

      {!isJobsLoading && !isError && filteredJobs.length > 0 && (
        <div className="flex flex-col gap-4">
          {filteredJobs.map((job) => (
            <JobCard key={job.id} job={job} profile={profile} />
          ))}
        </div>
      )}
    </div>
  );
}
