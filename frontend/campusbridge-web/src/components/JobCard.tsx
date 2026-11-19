import { Check, X } from 'lucide-react';
import type { ReactElement } from 'react';
import { evaluateEligibility } from '../features/eligibility/evaluateEligibility';
import type { Job, StudentProfile } from '../types';

interface JobCardProps {
  job: Job;
  profile?: StudentProfile | null;
}

export function JobCard({ job, profile }: JobCardProps): ReactElement {
  const formattedDeadline = new Date(
    `${job.deadline}T00:00:00`
  ).toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });

  const eligibility = profile ? evaluateEligibility(profile, job.rules) : null;

  return (
    <article className="rounded-lg border border-line bg-white p-5">
      <div className="flex flex-col justify-between gap-2 sm:flex-row sm:items-start">
        <div>
          <h2 className="text-lg font-bold text-ink">{job.title}</h2>
          <p className="text-sm text-ink/70">
            {job.company}, {job.location}
          </p>
        </div>
        <div className="shrink-0">
          <span className="text-lg font-bold text-brand">₹{job.packageLpa} LPA</span>
        </div>
      </div>

      <p className="mt-3 text-sm text-ink/80">{job.description}</p>

      <dl className="mt-4 grid grid-cols-2 gap-4 border-y border-line py-3 text-sm md:grid-cols-4">
        <div>
          <dt className="text-xs text-ink/60">Min CGPA</dt>
          <dd className="mt-0.5 font-semibold text-ink">{job.rules.minCgpa}</dd>
        </div>
        <div>
          <dt className="text-xs text-ink/60">Max backlogs</dt>
          <dd className="mt-0.5 font-semibold text-ink">{job.rules.maxBacklogs}</dd>
        </div>
        <div>
          <dt className="text-xs text-ink/60">Branches</dt>
          <dd className="mt-0.5 font-semibold text-ink">
            {job.rules.allowedBranches.join(', ')}
          </dd>
        </div>
        <div>
          <dt className="text-xs text-ink/60">Apply by</dt>
          <dd className="mt-0.5 font-semibold text-ink">{formattedDeadline}</dd>
        </div>
      </dl>

      <div className="mt-4 flex flex-wrap gap-2">
        {job.rules.requiredSkills.map((skill) => (
          <span
            key={skill}
            className="rounded-full bg-brand/10 px-3 py-1 text-xs font-medium text-brand-dark"
          >
            {skill}
          </span>
        ))}
      </div>

      {eligibility && (
        <div className="mt-5 border-t border-line pt-4">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-ink/60">
              Eligibility status
            </span>
            <span
              className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                eligibility.eligible
                  ? 'border border-emerald-200 bg-emerald-50 text-emerald-700'
                  : 'border border-alert/20 bg-alert/10 text-alert'
              }`}
            >
              {eligibility.eligible ? 'Eligible' : 'Not eligible'}
            </span>
          </div>

          <ul className="mt-3 flex flex-col gap-2">
            {eligibility.checks.map((check) => (
              <li key={check.rule} className="flex items-start gap-2 text-xs">
                {check.passed ? (
                  <Check className="mt-0.5 h-3.5 w-3.5 shrink-0 text-emerald-600" />
                ) : (
                  <X className="mt-0.5 h-3.5 w-3.5 shrink-0 text-alert" />
                )}
                <span
                  className={
                    check.passed ? 'text-ink/80' : 'font-medium text-ink'
                  }
                >
                  {check.message}
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </article>
  );
}
