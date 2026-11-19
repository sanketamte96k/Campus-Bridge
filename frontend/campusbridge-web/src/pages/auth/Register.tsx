import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import type { ReactElement } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { Button } from '../../components/Button';
import { Field } from '../../components/Field';
import { useAuth } from '../../features/auth/AuthContext';
import { homeFor } from '../../routes/paths';
import type { Role } from '../../types';

const registerSchema = z
  .object({
    name: z.string().min(2, 'Name must be at least 2 characters'),
    email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
    confirmPassword: z.string().min(1, 'Please confirm your password'),
    role: z.enum(['STUDENT', 'RECRUITER']),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords must match',
    path: ['confirmPassword'],
  });

type RegisterFormValues = z.infer<typeof registerSchema>;

export function Register(): ReactElement {
  const [serverError, setServerError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { register: authRegister } = useAuth();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      role: 'STUDENT',
    },
  });

  const selectedRole = useWatch({ control, name: 'role' });

  const onSubmit = async (values: RegisterFormValues): Promise<void> => {
    setServerError(null);
    setIsSubmitting(true);
    try {
      const user = await authRegister({
        name: values.name,
        email: values.email,
        password: values.password,
        role: values.role as Role,
      });
      navigate(homeFor(user.role));
    } catch (err) {
      setServerError(
        err instanceof Error ? err.message : 'An error occurred during registration.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="rounded-xl border border-line bg-white p-8 shadow-sm">
      <div className="mb-6">
        <h2 className="text-2xl font-bold tracking-tight text-ink">Create an account</h2>
        <p className="mt-1 text-sm text-ink/70">
          Get started with your campus placement portal.
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

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
        <div className="flex flex-col gap-1.5">
          <span className="text-sm font-medium text-ink">Select your role</span>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <label
              className={`flex cursor-pointer flex-col gap-1 rounded-lg border p-3.5 transition-all ${
                selectedRole === 'STUDENT'
                  ? 'border-brand bg-brand/5 ring-2 ring-brand/20'
                  : 'border-line bg-white hover:border-gray-400'
              }`}
            >
              <input
                type="radio"
                value="STUDENT"
                className="sr-only"
                {...register('role')}
              />
              <span className="text-sm font-semibold text-ink">Student</span>
              <span className="text-xs text-ink/70">
                Explore campus drives and check your eligibility.
              </span>
            </label>

            <label
              className={`flex cursor-pointer flex-col gap-1 rounded-lg border p-3.5 transition-all ${
                selectedRole === 'RECRUITER'
                  ? 'border-brand bg-brand/5 ring-2 ring-brand/20'
                  : 'border-line bg-white hover:border-gray-400'
              }`}
            >
              <input
                type="radio"
                value="RECRUITER"
                className="sr-only"
                {...register('role')}
              />
              <span className="text-sm font-semibold text-ink">Recruiter</span>
              <span className="text-xs text-ink/70">
                Post job drives and recruit campus candidates.
              </span>
            </label>
          </div>
          {errors.role?.message && (
            <span role="alert" className="text-xs text-alert">
              {errors.role.message}
            </span>
          )}
        </div>

        <Field
          label="Full name"
          type="text"
          placeholder="Jane Doe"
          autoComplete="name"
          error={errors.name?.message}
          registration={register('name')}
        />

        <Field
          label="Email address"
          type="email"
          placeholder="you@example.com"
          autoComplete="email"
          error={errors.email?.message}
          registration={register('email')}
        />

        <Field
          label="Password"
          type="password"
          placeholder="••••••••"
          autoComplete="new-password"
          error={errors.password?.message}
          registration={register('password')}
        />

        <Field
          label="Confirm password"
          type="password"
          placeholder="••••••••"
          autoComplete="new-password"
          error={errors.confirmPassword?.message}
          registration={register('confirmPassword')}
        />

        <Button type="submit" isLoading={isSubmitting} className="mt-2 w-full">
          Create account
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-ink/70">
        Already have an account?{' '}
        <Link to="/login" className="font-semibold text-brand hover:underline">
          Sign in
        </Link>
      </p>
    </div>
  );
}
