import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import type { ReactElement } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { Button } from '../../components/Button';
import { Field } from '../../components/Field';
import { useAuth } from '../../features/auth/AuthContext';
import { homeFor } from '../../routes/paths';

const loginSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export function Login(): ReactElement {
  const [serverError, setServerError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onSubmit = async (values: LoginFormValues): Promise<void> => {
    setServerError(null);
    setIsSubmitting(true);
    try {
      const user = await login(values);
      navigate(homeFor(user.role));
    } catch (err) {
      setServerError(
        err instanceof Error ? err.message : 'An error occurred during login.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="rounded-xl border border-line bg-white p-8 shadow-sm">
      <div className="mb-6">
        <h2 className="text-2xl font-bold tracking-tight text-ink">Sign in</h2>
        <p className="mt-1 text-sm text-ink/70">
          Enter your credentials to access your account.
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
          autoComplete="current-password"
          error={errors.password?.message}
          registration={register('password')}
        />

        <Button type="submit" isLoading={isSubmitting} className="mt-2 w-full">
          Sign in
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-ink/70">
        Do not have an account?{' '}
        <Link to="/register" className="font-semibold text-brand hover:underline">
          Register
        </Link>
      </p>
    </div>
  );
}
