import {
  Briefcase,
  ClipboardList,
  LayoutDashboard,
  SquarePlus,
  UserRound,
} from 'lucide-react';
import { createBrowserRouter } from 'react-router-dom';
import { AuthLayout } from '../layouts/AuthLayout';
import { DashboardLayout } from '../layouts/DashboardLayout';
import type { NavItem } from '../layouts/DashboardLayout';
import { Login } from '../pages/auth/Login';
import { Register } from '../pages/auth/Register';
import { Applicants } from '../pages/recruiter/Applicants';
import { RecruiterDashboard } from '../pages/recruiter/Dashboard';
import { MyJobs } from '../pages/recruiter/MyJobs';
import { PostJob } from '../pages/recruiter/PostJob';
import { Applications } from '../pages/student/Applications';
import { StudentDashboard } from '../pages/student/Dashboard';
import { Jobs } from '../pages/student/Jobs';
import { Profile } from '../pages/student/Profile';
import { AuthRedirect, ProtectedRoute } from './ProtectedRoute';

export const studentNav: NavItem[] = [
  { to: '/student', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/student/jobs', label: 'Jobs', icon: Briefcase, end: true },
  {
    to: '/student/applications',
    label: 'Applications',
    icon: ClipboardList,
    end: true,
  },
  { to: '/student/profile', label: 'Profile', icon: UserRound, end: true },
];

export const recruiterNav: NavItem[] = [
  {
    to: '/recruiter',
    label: 'Dashboard',
    icon: LayoutDashboard,
    end: true,
  },
  {
    to: '/recruiter/jobs',
    label: 'My jobs',
    icon: Briefcase,
    isActive: (pathname: string) =>
      pathname.startsWith('/recruiter/jobs') &&
      pathname !== '/recruiter/jobs/new',
  },
  {
    to: '/recruiter/jobs/new',
    label: 'Post job',
    icon: SquarePlus,
    end: true,
  },
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
        element: <DashboardLayout nav={studentNav} />,
        children: [
          { index: true, element: <StudentDashboard /> },
          { path: 'jobs', element: <Jobs /> },
          { path: 'applications', element: <Applications /> },
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
        element: <DashboardLayout nav={recruiterNav} />,
        children: [
          { index: true, element: <RecruiterDashboard /> },
          { path: 'jobs', element: <MyJobs /> },
          { path: 'jobs/new', element: <PostJob /> },
          { path: 'jobs/:jobId/applicants', element: <Applicants /> },
        ],
      },
    ],
  },
  {
    path: '*',
    element: <AuthRedirect />,
  },
]);
