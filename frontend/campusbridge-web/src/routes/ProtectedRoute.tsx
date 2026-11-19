import type { ReactElement } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../features/auth/AuthContext';
import type { Role } from '../types';
import { homeFor } from './paths';

interface ProtectedRouteProps {
  role: Role;
}

export function ProtectedRoute({ role }: ProtectedRouteProps): ReactElement {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (user.role !== role) {
    return <Navigate to={homeFor(user.role)} replace />;
  }

  return <Outlet />;
}

export function AuthRedirect(): ReactElement {
  const { user } = useAuth();
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  return <Navigate to={homeFor(user.role)} replace />;
}
