import type { ReactElement } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../features/auth/AuthContext';
import { homeFor } from '../routes/paths';

const CURRENT_YEAR = new Date().getFullYear();

export function AuthLayout(): ReactElement {
  const { user } = useAuth();

  if (user) {
    return <Navigate to={homeFor(user.role)} replace />;
  }

  return (
    <div className="flex min-h-screen flex-col lg:flex-row">
      <div className="flex flex-col justify-between bg-brand p-8 text-white lg:w-1/2 lg:p-16">
        <div>
          <span className="text-xl font-bold tracking-tight">CampusBridge</span>
        </div>
        <div className="my-12 lg:my-0">
          <h1 className="max-w-md text-3xl font-extrabold tracking-tight sm:text-4xl lg:text-5xl">
            Know if you're eligible before you apply.
          </h1>
          <p className="mt-4 max-w-sm text-sm text-white/80">
            Intelligent campus recruitment management and transparent placement workflows for students and recruiters.
          </p>
        </div>
        <div className="text-xs text-white/60">
          &copy; {CURRENT_YEAR} CampusBridge
        </div>
      </div>

      <div className="flex flex-1 items-center justify-center bg-paper p-6 sm:p-10 lg:w-1/2 lg:p-16">
        <div className="w-full max-w-md">
          <Outlet />
        </div>
      </div>
    </div>
  );
}

