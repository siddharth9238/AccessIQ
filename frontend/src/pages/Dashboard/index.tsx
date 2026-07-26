import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Users, CheckCircle, XCircle, Clock, AlertCircle, TrendingUp } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/api/client';
import { useAppSelector } from '@/store/hooks';
import { RoleName } from '@/types';
import { SkeletalChart } from '@/components/charts/SkeletalChart';

interface KPI {
  title: string;
  value: number | string;
  change?: string;
  icon: React.ElementType;
  color: string;
}

export default function Dashboard() {
  const { user, roles } = useAppSelector((state) => state.auth);

  const { data: stats, isLoading } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: async () => {
      const response = await apiClient.get('/api/v1/stats');
      return response.data;
    },
  });

  const kpis: KPI[] = [
    {
      title: 'Total Vendors',
      value: stats?.totalVendors ?? 0,
      change: '+12% this month',
      icon: Users,
      color: 'text-blue-600',
    },
    {
      title: 'Pending Approvals',
      value: stats?.pendingApprovals ?? 0,
      icon: Clock,
      color: 'text-yellow-600',
    },
    {
      title: 'Approved This Month',
      value: stats?.approvedVendors ?? 0,
      change: '+8% from last month',
      icon: CheckCircle,
      color: 'text-green-600',
    },
    {
      title: 'Rejected Vendors',
      value: stats?.rejectedVendors ?? 0,
      icon: XCircle,
      color: 'text-red-600',
    },
    {
      title: 'Compliance Rate',
      value: stats?.complianceRate ? `${stats.complianceRate}%` : '0%',
      icon: AlertCircle,
      color: 'text-purple-600',
    },
    {
      title: 'Risk Score',
      value: stats?.riskScore ?? 0,
      change: 'Low',
      icon: TrendingUp,
      color: 'text-orange-600',
    },
  ];

  const isAdmin = roles?.includes(RoleName.ADMIN);
  const isManager = roles?.includes(RoleName.MANAGER);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">
          Welcome back, {user?.firstName || user?.email.split('@')[0]}!
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {kpis.map((kpi) => (
          <Card key={kpi.title}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">{kpi.title}</CardTitle>
              <kpi.icon className={`h-4 w-4 ${kpi.color}`} />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{kpi.value}</div>
              {kpi.change && (
                <p className="text-xs text-muted-foreground">{kpi.change}</p>
              )}
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              <button className="w-full justify-start inline-flex items-center gap-2 rounded-md px-3 py-2 text-sm hover:bg-muted">
                <Plus className="h-4 w-4" />
                Create New Vendor
              </button>
              <button className="w-full justify-start inline-flex items-center gap-2 rounded-md px-3 py-2 text-sm hover:bg-muted">
                <FileText className="h-4 w-4" />
                Run Compliance Check
              </button>
              {isAdmin && (
                <button className="w-full justify-start inline-flex items-center gap-2 rounded-md px-3 py-2 text-sm hover:bg-muted">
                  <Shield className="h-4 w-4" />
                  Manage Roles
                </button>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="text-sm text-muted-foreground">No recent activity</div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Compliance Status</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div>
                <div className="flex justify-between text-sm">
                  <span>Compliant Vendors</span>
                  <span>85%</span>
                </div>
                <div className="w-full bg-muted rounded-full h-2">
                  <div className="bg-green-600 h-2 rounded-full" style={{ width: '85%' }} />
                </div>
              </div>
              <div>
                <div className="flex justify-between text-sm">
                  <span>Non-Compliant Vendors</span>
                  <span>15%</span>
                </div>
                <div className="w-full bg-muted rounded-full h-2">
                  <div className="bg-red-600 h-2 rounded-full" style={{ width: '15%' }} />
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

const Plus = (props: React.SVGProps<SVGSVGElement>) => (
  <svg {...props} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <line x1="12" y1="5" x2="12" y2="19" />
    <line x1="5" y1="12" x2="19" y2="12" />
  </svg>
);

const FileText = (props: React.SVGProps<SVGSVGElement>) => (
  <svg {...props} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
    <polyline points="14 2 14 8 20 8" />
  </svg>
);