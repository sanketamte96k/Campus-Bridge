import { Check, ChevronDown, X } from 'lucide-react';
import { useState } from 'react';
import type { ReactElement } from 'react';
import { evaluateEligibility } from '../features/eligibility/evaluateEligibility';
import { useLearningPlan, useSkillGap } from '../features/skillGap/useSkillGap';
import type { Application, Job, StudentProfile } from '../types';
import { Button } from './Button';
import { StatusBadge } from './StatusBadge';

interface JobCardProps {
  job: Job;
  profile?: StudentProfile | null;
  application?: Application;
  onApply?: () => void;
  applyError?: string | null;
  isApplying?: boolean;
}

export function JobCard({
  job,
  profile,
  application,
  onApply,
  applyError,
  isApplying = false,
}: JobCardProps): ReactElement {
  const formattedDeadline = new Date(
    `${job.deadline}T00:00:00`
  ).toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });

  const eligibility = profile ? evaluateEligibility(profile, job.rules) : null;
  const [currentTimestamp] = useState(() => Date.now());
  const [isGapExpanded, setIsGapExpanded] = useState(false);
  const deadlineEnd = new Date(`${job.deadline}T23:59:59`);
  const isDeadlinePassed = currentTimestamp > deadlineEnd.getTime();

  const isStudentNotEligible = Boolean(profile && eligibility && !eligibility.eligible);
  const { data: gap, isLoading: isGapLoading, isError: isGapError } = useSkillGap(
    job.id,
    isStudentNotEligible && isGapExpanded
  );
  const planMutation = useLearningPlan(job.id);

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

          {isStudentNotEligible && (
            <div className="mt-4 border-t border-line/60 pt-3">
              <button
                type="button"
                onClick={() => setIsGapExpanded((prev) => !prev)}
                aria-expanded={isGapExpanded}
                className="flex items-center gap-1.5 text-xs font-semibold text-brand hover:text-brand-dark cursor-pointer"
              >
                <span>See what's missing</span>
                <ChevronDown
                  className={`h-4 w-4 transition-transform duration-200 ${
                    isGapExpanded ? 'rotate-180' : ''
                  }`}
                />
              </button>

              {isGapExpanded && (
                <div className="mt-3 rounded-lg border border-line bg-paper/30 p-3.5">
                  {isGapLoading && (
                    <p className="text-xs text-ink/60">Loading skill gap...</p>
                  )}

                  {isGapError && (
                    <p className="text-xs text-alert">Couldn't load skill gap details. Try again later.</p>
                  )}

                  {gap && (
                    <div className="space-y-3">
                      {gap.matchedSkills.length > 0 && (
                        <div>
                          <p className="text-xs font-medium text-ink/70">Matched skills</p>
                          <div className="mt-1 flex flex-wrap gap-1.5">
                            {gap.matchedSkills.map((skill) => (
                              <span
                                key={skill}
                                className="inline-flex items-center rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-0.5 text-xs font-medium text-emerald-800"
                              >
                                {skill}
                              </span>
                            ))}
                          </div>
                        </div>
                      )}

                      {gap.missingSkills.length > 0 && (
                        <div>
                          <p className="text-xs font-medium text-ink/70">Missing skills</p>
                          <div className="mt-1 flex flex-wrap gap-1.5">
                            {gap.missingSkills.map((skill) => (
                              <span
                                key={skill}
                                className="inline-flex items-center rounded-full border border-amber-300 bg-amber-50 px-2.5 py-0.5 text-xs font-semibold text-amber-900"
                              >
                                {skill}
                              </span>
                            ))}
                          </div>
                        </div>
                      )}

                      <div className="space-y-1 text-xs text-ink/80">
                        {gap.cgpaGap > 0 && (
                          <p>You need {gap.cgpaGap} more CGPA to meet the minimum requirement.</p>
                        )}
                        {gap.backlogsOver > 0 && (
                          <p>You have {gap.backlogsOver} active backlogs over the maximum limit.</p>
                        )}
                        {!gap.branchAllowed && (
                          <p className="font-medium text-alert">This job isn't open to your branch</p>
                        )}
                        {gap.closeToEligible && (
                          <p className="font-medium text-brand-dark">
                            You are close to eligible for this role. Building the missing skills can help bridge the gap.
                          </p>
                        )}
                      </div>

                      {gap.missingSkills.length > 0 && (
                        <div className="border-t border-line/60 pt-3">
                          {!planMutation.data && (
                            <Button
                              variant="secondary"
                              onClick={() => planMutation.mutate()}
                              isLoading={planMutation.isPending}
                              loadingText="Creating your plan..."
                              disabled={planMutation.isPending}
                              className="text-xs"
                            >
                              Get a learning plan
                            </Button>
                          )}

                          {planMutation.isError && (
                            <p className="mt-2 text-xs font-medium text-alert">
                              {(() => {
                                const err = planMutation.error as { response?: { status?: number; data?: { message?: string } } };
                                if (err?.response?.status === 429) {
                                  return err.response?.data?.message || 'Rate limit exceeded. Maximum 10 learning plan requests per hour.';
                                }
                                return "Couldn't create a plan. Try again later.";
                              })()}
                            </p>
                          )}

                          {planMutation.data && (
                            <div className="mt-3 rounded-lg border border-line bg-white p-3.5 shadow-2xs">
                              <div className="flex items-center justify-between gap-2">
                                <span className="text-xs font-bold text-ink">Learning roadmap</span>
                                <span className="inline-flex items-center rounded-full border border-line bg-paper px-2 py-0.5 text-[10px] font-semibold text-ink/70 uppercase">
                                  {planMutation.data.aiGenerated ? 'AI-generated' : 'Template plan'}
                                </span>
                              </div>

                              <p className="mt-2 text-xs text-ink/80 leading-relaxed">
                                {planMutation.data.summary}
                              </p>

                              <div className="mt-3 space-y-2.5">
                                {planMutation.data.steps.map((step, idx) => (
                                  <div
                                    key={step.skill + idx}
                                    className="rounded-md border border-line bg-paper/50 p-2.5 text-xs"
                                  >
                                    <div className="flex items-center justify-between gap-2">
                                      <span className="font-semibold text-ink">{step.skill}</span>
                                      <span className="text-[11px] text-ink/60 shrink-0">
                                        {step.estimatedHours} hours estimated
                                      </span>
                                    </div>
                                    <p className="mt-1 text-ink/75 leading-relaxed">
                                      {step.whyItMatters}
                                    </p>
                                    {step.practiceIdeas.length > 0 && (
                                      <ul className="mt-2 list-disc list-inside space-y-0.5 text-[11px] text-ink/70">
                                        {step.practiceIdeas.map((idea, ideaIdx) => (
                                          <li key={ideaIdx}>{idea}</li>
                                        ))}
                                      </ul>
                                    )}
                                  </div>
                                ))}
                              </div>

                              <p className="mt-3 text-[11px] italic text-ink/50 border-t border-line/50 pt-2">
                                {planMutation.data.disclaimer}
                              </p>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Application / Action Footer */}
      {application ? (
        <div className="mt-5 flex items-center justify-between border-t border-line pt-4">
          <span className="text-xs font-semibold uppercase tracking-wider text-ink/60">
            Applied
          </span>
          <StatusBadge status={application.status} />
        </div>
      ) : isDeadlinePassed ? (
        <div className="mt-5 border-t border-line pt-4">
          <p className="text-sm font-medium text-ink/60">Applications closed</p>
        </div>
      ) : eligibility?.eligible ? (
        <div className="mt-5 border-t border-line pt-4">
          <Button
            onClick={onApply}
            isLoading={isApplying}
            loadingText="Applying..."
            disabled={isApplying}
          >
            Apply
          </Button>
          {applyError && (
            <div
              role="alert"
              className="mt-2 text-xs font-medium text-alert"
            >
              {applyError}
            </div>
          )}
        </div>
      ) : null}
    </article>
  );
}
