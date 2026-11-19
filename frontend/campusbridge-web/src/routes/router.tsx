import { Briefcase, LayoutDashboard, UserRound } from 'lucide-react';
import { createBrowserRouter } from 'react-router-dom';
import { AuthLayout } from '../layouts/AuthLayout';
import { DashboardLayout } from '../layouts/DashboardLayout';
import type { NavItem } from '../layouts/DashboardLayout';
import { Login } from '../pages/auth/Login';
import { Register } from '../pages/auth/Register';
import { RecruiterDashboard } from '../pages/recruiter/Dashboard';
import { StudentDashboard } from '../pages/student/Dashboard';
import { Jobs } from '../pages/student/Jobs';
import { Profile } from '../pages/student/Profile';
import { AuthRedirect, ProtectedRoute } from './ProtectedRoute';

export const studentNav: NavItem[] = [
  { to: '/student', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/student/jobs', label: 'Jobs', icon: Briefcase },
  { to: '/student/profile', label: 'Profile', icon: UserRound },
];

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AuthRedirect />,
  },
  {
    element: <AuthLayout />,
    children: [
      { path: 'login', element: <Login /> },
      { path: 'register', element: <Register /> },
    ],
  },
  {
    path: 'student',
    element: <ProtectedRoute role="STUDENT" />,
    children: [
      {
        element: <DashboardLayout navItems={studentNav} />,
        children: [
          { index: true, element: <StudentDashboard /> },
          { path: 'jobs', element: <Jobs /> },
          { path: 'profile', element: <Profile /> },
        ],
      },
    ],
  },
  {
    path: 'recruiter',
    element: <ProtectedRoute role="RECRUITER" />,
    children: [
      {
        element: <DashboardLayout />,
        children: [{ index: true, element: <RecruiterDashboard /> }],
      },
    ],
  },
  {
    path: '*',
    element: <AuthRedirect />,
  },
]);
