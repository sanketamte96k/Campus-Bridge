import { createBrowserRouter } from 'react-router-dom';
import { AuthLayout } from '../layouts/AuthLayout';
import { DashboardLayout } from '../layouts/DashboardLayout';
import { Login } from '../pages/auth/Login';
import { Register } from '../pages/auth/Register';
import { RecruiterDashboard } from '../pages/recruiter/Dashboard';
import { StudentDashboard } from '../pages/student/Dashboard';
import { AuthRedirect, ProtectedRoute } from './ProtectedRoute';

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
        element: <DashboardLayout />,
        children: [{ index: true, element: <StudentDashboard /> }],
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
