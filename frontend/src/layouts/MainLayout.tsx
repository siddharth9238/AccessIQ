import { Link, Outlet, useLocation } from 'react-router-dom';
import { useState } from 'react';
import { Menu, X, Bell, Search, Moon, Sun, User, Settings, LogOut, BarChart3, FileText, Users, Shield } from 'lucide-react';
import { useAppSelector, useAppDispatch } from '@/store/hooks';
import { clearAuth } from '@/store/slices/authSlice';
import { toggleTheme } from '@/store/slices/themeSlice';
import { cn } from '@/utils';

const navigation = [
  { name: 'Dashboard', href: '/', icon: BarChart3 },
  { name: 'Vendors', href: '/vendors', icon: Users },
  { name: 'Compliance', href: '/compliance', icon: Shield },
  { name: 'Reports', href: '/reports', icon: FileText },
  { name: 'Users', href: '/users', icon: User },
  { name: 'Settings', href: '/settings', icon: Settings },
];

export default function MainLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);
  const { user, roles } = useAppSelector((state) => state.auth);
  const { mode } = useAppSelector((state) => state.theme);
  const dispatch = useAppDispatch();

  const isAdmin = roles?.includes('ADMIN');
  const location = useLocation();

  const handleLogout = () => {
    dispatch(clearAuth());
  };

  return (
    <div className="flex h-screen bg-background">
      {/* Mobile sidebar */}
      <div className="fixed inset-0 z-40 lg:hidden">
        {sidebarOpen && (
          <div className="fixed inset-0 bg-background/80 backdrop-blur-sm" onClick={() => setSidebarOpen(false)} />
        )}
        <div className="fixed left-0 top-0 z-50 flex h-screen w-64 flex-col bg-card border-r border-border">
          <div className="flex h-16 items-center justify-between px-4 border-b border-border">
            <Link to="/" className="text-xl font-bold text-primary">
              AccessIQ
            </Link>
            <button
              onClick={() => setSidebarOpen(false)}
              className="rounded-full p-2 hover:bg-muted"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
          <nav className="flex-1 overflow-y-auto p-2">
            {navigation.map((item) => {
              const Icon = item.icon;
              return (
                <Link
                  key={item.name}
                  to={item.href}
                  onClick={() => setSidebarOpen(false)}
                  className={cn(
                    'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium hover:bg-muted',
                    location.pathname === item.href ? 'bg-muted text-primary' : 'text-foreground'
                  )}
                >
                  <Icon className="h-4 w-4" />
                  {item.name}
                </Link>
              );
            })}
          </nav>
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className="hidden lg:flex lg:w-64 lg:flex-col lg:fixed lg:inset-y-0">
        <div className="flex flex-col flex-grow min-h-0 bg-card border-r border-border">
          <div className="flex items-center justify-between h-16 px-4 border-b border-border">
            <Link to="/" className="text-xl font-bold text-primary">
              AccessIQ
            </Link>
          </div>
          <nav className="flex-1 overflow-y-auto px-2 pb-4">
            {navigation.map((item) => {
              const Icon = item.icon;
              return (
                <Link
                  key={item.name}
                  to={item.href}
                  className={cn(
                    'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium hover:bg-muted',
                    location.pathname === item.href ? 'bg-muted text-primary' : 'text-foreground'
                  )}
                >
                  <Icon className="h-4 w-4" />
                  {item.name}
                </Link>
              );
            })}
          </nav>
        </div>
      </div>

      {/* Main content */}
      <div className="flex flex-col flex-1 lg:pl-64">
        <header className="flex items-center justify-between h-16 px-4 border-b border-border bg-card">
          <button
            onClick={() => setSidebarOpen(true)}
            className="lg:hidden rounded-full p-2 hover:bg-muted"
          >
            <Menu className="h-5 w-5" />
          </button>

          <div className="flex items-center gap-4">
            <div className={cn('relative', searchOpen ? 'w-full max-w-sm' : 'w-auto')}>
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <input
                type="text"
                placeholder="Search..."
                className={cn(
                  'pl-10 pr-4 py-2 text-sm border border-input rounded-lg focus:outline-none focus:ring-2 focus:ring-ring bg-background',
                  searchOpen ? 'w-full' : 'w-0 md:w-auto'
                )}
                onFocus={() => setSearchOpen(true)}
                onBlur={() => setSearchOpen(false)}
              />
            </div>

            <button
              onClick={() => dispatch(toggleTheme())}
              className="rounded-full p-2 hover:bg-muted"
            >
              {mode === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </button>

            <button className="relative rounded-full p-2 hover:bg-muted">
              <Bell className="h-5 w-5" />
              <span className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-destructive text-xs text-destructive-foreground">
                3
              </span>
            </button>

            <Link
              to="/profile"
              className="hidden items-center gap-2 rounded-full p-2 hover:bg-muted md:flex"
            >
              <User className="h-5 w-5" />
              <span className="hidden md:block">{user?.firstName || user?.email}</span>
            </Link>

            <button
              onClick={handleLogout}
              className="rounded-full p-2 hover:bg-muted"
              title="Logout"
            >
              <LogOut className="h-5 w-5" />
            </button>
          </div>
        </header>

        <main className="flex-1 overflow-y-auto p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}