import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import type { ReactElement } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { Button } from '../../components/Button';
import { Field } from '../../components/Field';
import { useCreateJob } from '../../features/recruiter/useRecruiter';
import type { Branch, NewJobInput } from '../../types';

const BRANCH_OPTIONS: Branch[] = ['CS', 'IT', 'ENTC', 'MECH', 'CIVIL'];

const postJobSchema = z.object({
  title: z.string().trim().min(2, 'Enter a job title.'),
  company: z.string().trim().min(2, 'Enter the company name.'),
  location: z.string().trim().min(2, 'Enter the work location.'),
  packageLpa: z
    .string()
    .trim()
    .min(1, 'Enter the package in LPA.')
    .refine(
      (v) => !isNaN(Number(v)) && Number(v) > 0,
      'Enter a package greater than 0.'
    ),
  deadline: z
    .string()
    .trim()
    .min(1, 'Select an application deadline.')
    .refine((val) => {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const chosen = new Date(`${val}T00:00:00`);
      return chosen.getTime() >= today.getTime();
    }, 'Deadline must be today or later.'),
  description: z
    .string()
    .trim()
    .min(10, 'Enter a description with at least 10 characters.'),
  allowedBranches: z
    .array(z.enum(['CS', 'IT', 'ENTC', 'MECH', 'CIVIL']))
    .min(1, 'Select at least one allowed branch.'),
  minCgpa: z
    .string()
    .trim()
    .min(1, 'Enter the minimum CGPA.')
    .refine(
      (v) => !isNaN(Number(v)) && Number(v) >= 0 && Number(v) <= 10,
      'Enter a CGPA between 0 and 10.'
    ),
  maxBacklogs: z
    .string()
    .trim()
    .min(1, 'Enter maximum allowed backlogs.')
    .refine(
      (v) => !isNaN(Number(v)) && Number.isInteger(Number(v)) && Number(v) >= 0,
      'Enter 0 or a positive whole number.'
    ),
  requiredSkills: z
    .string()
    .trim()
    .min(1, 'Enter at least one required skill.'),
});

type PostJobFormValues = z.infer<typeof postJobSchema>;

export function PostJob(): ReactElement {
  const navigate = useNavigate();
  const createJobMutation = useCreateJob();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<PostJobFormValues>({
    resolver: zodResolver(postJobSchema),
    defaultValues: {
      title: '',
      company: '',
      location: '',
      packageLpa: '',
      deadline: '',
      description: '',
      allowedBranches: ['CS', 'IT'],
      minCgpa: '7.0',
      maxBacklogs: '0',
      requiredSkills: '',
    },
  });

  const selectedBranches =
    useWatch({ control, name: 'allowedBranches' }) ?? [];

  const onSubmit = (values: PostJobFormValues): void => {
    setServerError(null);

    const skillsArray = Array.from(
      new Set(
        values.requiredSkills
          .split(',')
          .map((s) => s.trim())
          .filter((s) => s.length > 0)
      )
    );

    const input: NewJobInput = {
      title: values.title.trim(),
      company: values.company.trim(),
      location: values.location.trim(),
      packageLpa: parseFloat(Number(values.packageLpa).toFixed(2)),
      deadline: values.deadline,
      description: values.description.trim(),
      rules: {
        allowedBranches: values.allowedBranches,
        minCgpa: parseFloat(Number(values.minCgpa).toFixed(2)),
        maxBacklogs: parseInt(values.maxBacklogs, 10),
        requiredSkills: skillsArray,
      },
    };

    createJobMutation.mutate(input, {
      onSuccess: () => {
        navigate('/recruiter/jobs');
      },
      onError: (err) => {
        setServerError(
          err instanceof Error ? err.message : 'Could not create job posting.'
        );
      },
    });
  };

  return (
    <div className="max-w-2xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">
          Post a job
        </h1>
        <p className="mt-1 text-sm text-ink/70">
          Create a campus placement drive and configure eligibility criteria.
        </p>
      </div>

      {serverError && (
        <div
          role="alert"
          className="mb-4 rounded-md border border-alert/20 bg-alert/10 p-3 text-sm text-alert"
        >
          {serverError}
        </div>
      )}

      <form
        onSubmit={handleSubmit(onSubmit)}
        className="flex flex-col gap-5 rounded-xl border border-line bg-white p-6 shadow-xs sm:p-8"
        noValidate
      >
        <Field
          label="Job title"
          placeholder="Software engineer"
          error={errors.title?.message}
          registration={register('title')}
        />

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Company"
            placeholder="Acme Corp"
            error={errors.company?.message}
            registration={register('company')}
          />

          <Field
            label="Location"
            placeholder="Pune, Maharashtra"
            error={errors.location?.message}
            registration={register('location')}
          />
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Package (LPA)"
            type="number"
            step="0.1"
            min="0.1"
            placeholder="8.5"
            error={errors.packageLpa?.message}
            registration={register('packageLpa')}
          />

          <Field
            label="Application deadline"
            type="date"
            error={errors.deadline?.message}
            registration={register('deadline')}
          />
        </div>

        <div className="flex flex-col gap-1.5">
          <label htmlFor="description" className="text-sm font-medium text-ink">
            Description
          </label>
          <textarea
            id="description"
            rows={3}
            placeholder="Describe role responsibilities, team environment, and expectations."
            className={`w-full rounded-md border bg-white px-3 py-2 text-sm text-ink placeholder:text-gray-400 focus:outline-none focus:ring-2 transition-colors ${
              errors.description
                ? 'border-alert focus:border-alert focus:ring-alert/20'
                : 'border-line focus:border-brand focus:ring-brand/20'
            }`}
            {...register('description')}
          />
          {errors.description?.message && (
            <span role="alert" className="text-xs text-alert">
              {errors.description.message}
            </span>
          )}
        </div>

        {/* Allowed branches checkbox group */}
        <fieldset className="flex flex-col gap-1.5 border-t border-line pt-4">
          <legend className="text-sm font-medium text-ink">
            Allowed branches
          </legend>
          <div className="mt-1 flex flex-wrap gap-4">
            {BRANCH_OPTIONS.map((branch) => {
              const isChecked = selectedBranches.includes(branch);
              return (
                <label
                  key={branch}
                  className="flex cursor-pointer items-center gap-2 text-sm text-ink"
                >
                  <input
                    type="checkbox"
                    value={branch}
                    className="h-4 w-4 rounded border-line text-brand focus:ring-brand"
                    {...register('allowedBranches')}
                  />
                  <span className={isChecked ? 'font-semibold text-brand' : ''}>
                    {branch}
                  </span>
                </label>
              );
            })}
          </div>
          {errors.allowedBranches?.message && (
            <span role="alert" className="text-xs text-alert">
              {errors.allowedBranches.message}
            </span>
          )}
        </fieldset>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Minimum CGPA"
            type="number"
            step="0.1"
            min="0"
            max="10"
            placeholder="7.0"
            error={errors.minCgpa?.message}
            registration={register('minCgpa')}
          />

          <Field
            label="Maximum active backlogs"
            type="number"
            step="1"
            min="0"
            placeholder="0"
            error={errors.maxBacklogs?.message}
            registration={register('maxBacklogs')}
          />
        </div>

        <div className="flex flex-col gap-1.5">
          <Field
            label="Required skills"
            type="text"
            placeholder="Java, SQL, Spring Boot"
            error={errors.requiredSkills?.message}
            registration={register('requiredSkills')}
          />
          <span className="text-xs text-ink/60">
            Enter skills separated by commas.
          </span>
        </div>

        <div className="pt-2">
          <Button
            type="submit"
            isLoading={createJobMutation.isPending}
            loadingText="Posting job..."
            className="w-full sm:w-auto"
          >
            Post job
          </Button>
        </div>
      </form>
    </div>
  );
}
