import type { ReactElement } from 'react';
import { useAuth } from '../../features/auth/AuthContext';

export function RecruiterDashboard(): ReactElement {
  const { user } = useAuth();

  return (
    <div className="max-w-4xl">
      <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">
        Welcome, {user?.name}
      </h1>
      <p className="mt-2 text-sm text-ink/70">
        Manage your company placement drives, review shortlisted applicants, and schedule interview rounds.
      </p>
    </div>
  );
}
