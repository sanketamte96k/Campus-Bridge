import type { ReactElement } from 'react';
import type { ApplicationStatus } from '../types';

interface StatusBadgeProps {
  status: ApplicationStatus;
  className?: string;
}

function getStatusBadgeClasses(status: ApplicationStatus): string {
  switch (status) {
    case 'APPLIED':
      return 'border border-blue-200 bg-blue-50 text-blue-700';
    case 'SHORTLISTED':
      return 'border border-amber-200 bg-amber-50 text-amber-700';
    case 'SELECTED':
      return 'border border-emerald-200 bg-emerald-50 text-emerald-700';
    case 'REJECTED':
      return 'border border-alert/20 bg-alert/10 text-alert';
    case 'WITHDRAWN':
      return 'border border-gray-200 bg-gray-100 text-gray-700';
    default:
      return 'border border-line bg-paper text-ink';
  }
}

function getStatusLabel(status: ApplicationStatus): string {
  switch (status) {
    case 'APPLIED':
      return 'Applied';
    case 'SHORTLISTED':
      return 'Shortlisted';
    case 'SELECTED':
      return 'Selected';
    case 'REJECTED':
      return 'Rejected';
    case 'WITHDRAWN':
      return 'Withdrawn';
    default:
      return status;
  }
}

export function StatusBadge({
  status,
  className = '',
}: StatusBadgeProps): ReactElement {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${getStatusBadgeClasses(
        status
      )} ${className}`}
    >
      {getStatusLabel(status)}
    </span>
  );
}
