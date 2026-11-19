import { Briefcase, LayoutDashboard, LogOut, UserRound } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import type { ComponentType, ReactElement } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../features/auth/AuthContext';

export interface NavItem {
  to: string;
  label: string;
  icon: LucideIcon | ComponentType<{ className?: string }>;
}

interface DashboardLayoutProps {
  navItems?: NavItem[];
}

export function DashboardLayout({ navItems }: DashboardLayoutProps = {}): ReactElement {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = (): void => {
    logout();
    navigate('/login');
  };

  const roleTitle = user?.role === 'STUDENT' ? 'Student portal' : 'Recruiter portal';

  const defaultItems: NavItem[] =
    user?.role === 'STUDENT'
      ? [
          { to: '/student', label: 'Dashboard', icon: LayoutDashboard },
          { to: '/student/jobs', label: 'Jobs', icon: Briefcase },
          { to: '/student/profile', label: 'Profile', icon: UserRound },
        ]
      : [{ to: '/recruiter', label: 'Dashboard', icon: LayoutDashboard }];

  const items = navItems ?? defaultItems;

  return (
    <div className="flex min-h-screen flex-col md:flex-row">
      {/* Mobile top bar */}
      <header className="flex items-center justify-between border-b border-line bg-white px-4 py-3 md:hidden">
        <div>
          <span className="text-base font-bold text-ink">CampusBridge</span>
          <span className="ml-2 text-xs font-medium text-ink/60">{roleTitle}</span>
        </div>
        <div className="flex items-center gap-3">
          <nav className="flex items-center gap-1">
            {items.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === '/student' || item.to === '/recruiter'}
                  className={({ isActive }) =>
                    `flex items-center gap-1.5 rounded-md px-2.5 py-1.5 text-xs font-medium transition-colors ${
                      isActive
                        ? 'bg-brand/10 text-brand font-semibold'
                        : 'text-ink hover:bg-paper'
                    }`
                  }
                >
                  <Icon className="h-4 w-4" />
                  <span>{item.label}</span>
                </NavLink>
              );
            })}
          </nav>
          <span className="text-xs font-medium text-ink">{user?.name}</span>
          <button
            type="button"
            onClick={handleLogout}
            aria-label="Log out"
            title="Log out"
            className="rounded p-1.5 text-ink/70 hover:bg-paper hover:text-alert focus:outline-none transition-colors"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      </header>

      {/* Desktop sidebar */}
      <aside className="hidden w-64 shrink-0 flex-col justify-between border-r border-line bg-white p-6 md:flex">
        <div>
          <div>
            <span className="text-xl font-bold tracking-tight text-ink">CampusBridge</span>
            <p className="text-xs font-medium text-ink/60">{roleTitle}</p>
          </div>

          <nav className="mt-8 flex flex-col gap-1.5">
            {items.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === '/student' || item.to === '/recruiter'}
                  className={({ isActive }) =>
                    `flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                      isActive
                        ? 'bg-brand/10 text-brand font-semibold'
                        : 'text-ink hover:bg-paper'
                    }`
                  }
                >
                  <Icon className="h-4 w-4" />
                  <span>{item.label}</span>
                </NavLink>
              );
            })}
          </nav>
        </div>

        <div className="border-t border-line pt-4">
          <div className="flex items-center justify-between">
            <div className="min-w-0 pr-2">
              <p className="truncate text-sm font-medium text-ink">{user?.name}</p>
              <p className="truncate text-xs text-ink/60">{user?.email}</p>
            </div>
            <button
              type="button"
              onClick={handleLogout}
              aria-label="Log out"
              title="Log out"
              className="rounded p-1.5 text-ink/70 hover:bg-paper hover:text-alert focus:outline-none transition-colors"
            >
              <LogOut className="h-4 w-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 bg-paper p-6 sm:p-8 md:p-10">
        <Outlet />
      </main>
    </div>
  );
}
