import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import type { ReactElement } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { Button } from '../../components/Button';
import { Field } from '../../components/Field';
import { useAuth } from '../../features/auth/AuthContext';
import { useProfile } from '../../features/profile/useProfile';
import { saveProfile } from '../../services/profileService';
import type { Branch, StudentProfile } from '../../types';

const BRANCH_OPTIONS: Branch[] = ['CS', 'IT', 'ENTC', 'MECH', 'CIVIL'];

const profileSchema = z.object({
  branch: z.enum(['CS', 'IT', 'ENTC', 'MECH', 'CIVIL']),
  cgpa: z
    .string()
    .trim()
    .min(1, 'Enter your CGPA.')
    .refine((val) => !isNaN(Number(val)), 'Enter a valid number.')
    .refine((val) => {
      const num = Number(val);
      return num >= 0 && num <= 10;
    }, 'Enter a CGPA between 0 and 10.'),
  activeBacklogs: z
    .string()
    .trim()
    .min(1, 'Enter your active backlogs.')
    .refine((val) => !isNaN(Number(val)), 'Enter a valid number.')
    .refine(
      (val) => Number.isInteger(Number(val)) && Number(val) >= 0,
      'Enter 0 or a positive whole number.'
    ),
  skills: z.string(),
});

type ProfileFormValues = z.infer<typeof profileSchema>;

export function Profile(): ReactElement {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const { profile, isLoading } = useProfile();
  const [saveSuccess, setSaveSuccess] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ProfileFormValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      branch: 'IT',
      cgpa: '',
      activeBacklogs: '0',
      skills: '',
    },
  });

  useEffect(() => {
    if (profile) {
      reset({
        branch: profile.branch,
        cgpa: String(profile.cgpa),
        activeBacklogs: String(profile.activeBacklogs),
        skills: profile.skills.join(', '),
      });
    }
  }, [profile, reset]);

  const mutation = useMutation({
    mutationFn: (newProfile: StudentProfile) => {
      if (!user) {
        throw new Error('User not authenticated.');
      }
      return saveProfile(user.id, newProfile);
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['profile', user?.id] });
      setSaveSuccess(true);
    },
  });

  const onSubmit = (values: ProfileFormValues): void => {
    setSaveSuccess(false);

    const parsedSkills = Array.from(
      new Set(
        values.skills
          .split(',')
          .map((s) => s.trim())
          .filter((s) => s.length > 0)
      )
    );

    const studentProfile: StudentProfile = {
      branch: values.branch,
      cgpa: parseFloat(Number(values.cgpa).toFixed(2)),
      activeBacklogs: parseInt(values.activeBacklogs, 10),
      skills: parsedSkills,
    };

    mutation.mutate(studentProfile);
  };

  return (
    <div className="max-w-2xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">
          Your profile
        </h1>
        <p className="mt-1 text-sm text-ink/70">
          Eligibility checks use these details.
        </p>
      </div>

      {isLoading ? (
        <div className="py-8 text-sm text-ink/70">Loading profile...</div>
      ) : (
        <form
          onSubmit={handleSubmit(onSubmit)}
          className="rounded-xl border border-line bg-white p-6 shadow-xs sm:p-8 flex flex-col gap-5"
          noValidate
        >
          {/* Branch select */}
          <div className="flex flex-col gap-1.5">
            <label htmlFor="branch" className="text-sm font-medium text-ink">
              Branch
            </label>
            <select
              id="branch"
              className={`w-full rounded-md border bg-white px-3 py-2 text-sm text-ink focus:outline-none focus:ring-2 transition-colors ${
                errors.branch
                  ? 'border-alert focus:border-alert focus:ring-alert/20'
                  : 'border-line focus:border-brand focus:ring-brand/20'
              }`}
              {...register('branch')}
            >
              {BRANCH_OPTIONS.map((branch) => (
                <option key={branch} value={branch}>
                  {branch}
                </option>
              ))}
            </select>
            {errors.branch?.message && (
              <span role="alert" className="text-xs text-alert">
                {errors.branch.message}
              </span>
            )}
          </div>

          {/* CGPA */}
          <Field
            label="CGPA"
            type="number"
            step="0.01"
            min="0"
            max="10"
            placeholder="8.2"
            error={errors.cgpa?.message}
            registration={register('cgpa')}
          />

          {/* Active Backlogs */}
          <Field
            label="Active backlogs"
            type="number"
            step="1"
            min="0"
            placeholder="0"
            error={errors.activeBacklogs?.message}
            registration={register('activeBacklogs')}
          />

          {/* Skills */}
          <div className="flex flex-col gap-1.5">
            <Field
              label="Skills"
              type="text"
              placeholder="Java, SQL, React"
              error={errors.skills?.message}
              registration={register('skills')}
            />
            <span className="text-xs text-ink/60">
              Enter skills separated by commas.
            </span>
          </div>

          {mutation.isError && (
            <div
              role="alert"
              className="rounded-md border border-alert/20 bg-alert/10 p-3 text-sm text-alert"
            >
              Could not save profile. Please try again.
            </div>
          )}

          <div className="pt-2">
            <Button type="submit" isLoading={mutation.isPending}>
              Save profile
            </Button>
            {saveSuccess && (
              <p className="mt-2 text-sm font-medium text-emerald-600">
                Profile saved
              </p>
            )}
          </div>
        </form>
      )}
    </div>
  );
}
