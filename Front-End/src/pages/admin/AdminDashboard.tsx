import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import {
  Users, Headphones, Clock, AlertTriangle, UserCheck, Activity,
} from 'lucide-react'
import { adminProviderApi } from '@/api/provider'
import { adminTicketApi } from '@/api/tickets'
import { adminApi } from '@/api/admin'
import { userApi } from '@/api/user'
import { formatRelative } from '@/utils/formatters'

export default function AdminDashboard() {
  // Query providers (real backend)
  const { data: providersData } = useQuery({
    queryKey: ['admin', 'dashboard', 'providers'],
    queryFn: () => adminProviderApi.getAll({ size: 50 }),
    select: (r) => r.data?.content ?? [],
  })

  // Query tickets (real backend)
  const { data: ticketsData } = useQuery({
    queryKey: ['admin', 'dashboard', 'tickets'],
    queryFn: () => adminTicketApi.getAll({ size: 50 }),
    select: (r) => r.data?.content ?? [],
  })

  // Query recent audit logs (real backend)
  const { data: auditData } = useQuery({
    queryKey: ['admin', 'dashboard', 'audit'],
    queryFn: () => adminApi.getAuditLogs({ size: 6 }),
    select: (r) => r.data?.content ?? [],
  })

  // Query registered customers / users (real backend)
  const { data: usersData } = useQuery({
    queryKey: ['admin', 'dashboard', 'users'],
    queryFn: () => userApi.getAll(),
    select: (r) => r.data ?? [],
  })

  const providers = providersData ?? []
  const pendingProviders = providers.filter((p) => p.status === 'PENDING')
  const approvedProviders = providers.filter((p) => p.status === 'APPROVED')

  const tickets = ticketsData ?? []
  const openTickets = tickets.filter((t) => t.status === 'OPEN' || t.status === 'IN_PROGRESS')
  const urgentTickets = openTickets.filter((t) => t.priority === 'URGENT' || t.priority === 'HIGH')

  const auditLogs = auditData ?? []
  const totalUsers = usersData?.length ?? 0

  return (
    <div className="space-y-8 max-w-6xl">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-blue-950 to-indigo-950 rounded-3xl p-6 sm:p-8 text-white shadow-lg relative overflow-hidden">
        <div className="space-y-1">
          <span className="text-xs font-semibold uppercase tracking-wider text-blue-300">
            System Administration
          </span>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
            Administrator Command Center
          </h1>
          <p className="text-xs sm:text-sm text-slate-300">
            Platform governance, provider approvals, support escalation, and security audit trail.
          </p>
        </div>
      </div>

      {/* KPI Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <Link
          to="/admin/providers"
          className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm hover:border-blue-400 transition-all flex items-center gap-4"
        >
          <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center flex-shrink-0">
            <Users className="w-6 h-6" />
          </div>
          <div>
            <p className="text-2xl font-black text-slate-900">{providers.length}</p>
            <p className="text-xs font-medium text-slate-500">Total Providers</p>
            <p className="text-[11px] text-emerald-600 font-semibold mt-0.5">
              {approvedProviders.length} active
            </p>
          </div>
        </Link>

        <Link
          to="/admin/providers"
          className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm hover:border-amber-400 transition-all flex items-center gap-4"
        >
          <div className="w-12 h-12 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center flex-shrink-0">
            <Clock className="w-6 h-6" />
          </div>
          <div>
            <p className="text-2xl font-black text-slate-900">{pendingProviders.length}</p>
            <p className="text-xs font-medium text-slate-500">Pending Approvals</p>
            <p className="text-[11px] text-amber-600 font-semibold mt-0.5">Needs Review</p>
          </div>
        </Link>

        <Link
          to="/admin/tickets"
          className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm hover:border-rose-400 transition-all flex items-center gap-4"
        >
          <div className="w-12 h-12 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center flex-shrink-0">
            <Headphones className="w-6 h-6" />
          </div>
          <div>
            <p className="text-2xl font-black text-slate-900">{openTickets.length}</p>
            <p className="text-xs font-medium text-slate-500">Open Tickets</p>
            <p className="text-[11px] text-rose-600 font-semibold mt-0.5">
              {urgentTickets.length} high priority
            </p>
          </div>
        </Link>

        <Link
          to="/admin/customers"
          className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm hover:border-purple-400 transition-all flex items-center gap-4"
        >
          <div className="w-12 h-12 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center flex-shrink-0">
            <UserCheck className="w-6 h-6" />
          </div>
          <div>
            <p className="text-2xl font-black text-slate-900">{totalUsers}</p>
            <p className="text-xs font-medium text-slate-500">Registered Users</p>
            <p className="text-[11px] text-purple-600 font-semibold mt-0.5">Customer Accounts</p>
          </div>
        </Link>
      </div>

      {/* Pending Provider Approvals Table/Queue */}
      {pendingProviders.length > 0 && (
        <div className="bg-amber-50/60 border border-amber-200/80 rounded-3xl p-6 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-amber-900">
              <AlertTriangle className="w-5 h-5 text-amber-600" />
              <h2 className="text-base font-bold">
                Providers Awaiting Verification ({pendingProviders.length})
              </h2>
            </div>
            <Link
              to="/admin/providers"
              className="text-xs font-bold text-amber-800 hover:text-amber-900"
            >
              Review All →
            </Link>
          </div>

          <div className="space-y-3">
            {pendingProviders.slice(0, 4).map((p) => (
              <div
                key={p.id}
                className="bg-white rounded-2xl p-4 border border-amber-200 shadow-sm flex items-center justify-between gap-4"
              >
                <div>
                  <h4 className="text-sm font-bold text-slate-900">{p.businessName}</h4>
                  <p className="text-xs text-slate-500 mt-0.5">
                    {p.email} · {p.city || 'Bangalore'}
                  </p>
                </div>
                <Link
                  to={`/admin/providers/${p.id}`}
                  className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold shadow-sm transition-all"
                >
                  Verify Application
                </Link>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Grid: Support Tickets & Audit Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Support Tickets Queue */}
        <div className="bg-white rounded-3xl border border-slate-200/80 shadow-sm overflow-hidden flex flex-col">
          <div className="p-6 border-b border-slate-100 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <Headphones className="w-5 h-5 text-blue-600" />
              <h3 className="text-base font-bold text-slate-900">Active Support Cases</h3>
            </div>
            <Link to="/admin/tickets" className="text-xs font-bold text-blue-600 hover:text-blue-700">
              View All
            </Link>
          </div>

          <div className="p-4 flex-1 space-y-3">
            {openTickets.length === 0 ? (
              <p className="text-xs text-slate-400 text-center py-8">
                No unresolved support tickets at this time.
              </p>
            ) : (
              openTickets.slice(0, 5).map((t) => (
                <Link
                  key={t.id}
                  to={`/admin/tickets/${t.id}`}
                  className="p-3.5 rounded-xl border border-slate-100 hover:border-slate-200 hover:bg-slate-50 transition-all flex items-center justify-between gap-2 block"
                >
                  <div className="space-y-0.5 truncate">
                    <p className="text-xs font-bold text-slate-900 truncate">{t.subject}</p>
                    <p className="text-[11px] text-slate-400">
                      #{t.ticketNumber} · Priority: {t.priority}
                    </p>
                  </div>
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-blue-50 text-blue-700">
                    {t.status}
                  </span>
                </Link>
              ))
            )}
          </div>
        </div>

        {/* Audit Activity Stream */}
        <div className="bg-white rounded-3xl border border-slate-200/80 shadow-sm overflow-hidden flex flex-col">
          <div className="p-6 border-b border-slate-100 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <Activity className="w-5 h-5 text-indigo-600" />
              <h3 className="text-base font-bold text-slate-900">Recent Audit Log Events</h3>
            </div>
            <Link to="/admin/audit-logs" className="text-xs font-bold text-blue-600 hover:text-blue-700">
              Full Logs
            </Link>
          </div>

          <div className="p-4 flex-1 space-y-3">
            {auditLogs.length === 0 ? (
              <p className="text-xs text-slate-400 text-center py-8">
                No audit log records recorded yet.
              </p>
            ) : (
              auditLogs.slice(0, 5).map((log) => (
                <div
                  key={log.id}
                  className="p-3.5 rounded-xl border border-slate-100 flex items-center justify-between gap-2 text-xs"
                >
                  <div>
                    <p className="font-bold text-slate-800">{log.action}</p>
                    <p className="text-[11px] text-slate-400">
                      Actor #{log.actorId} ({log.actorRole}) · {log.resourceType}
                    </p>
                  </div>
                  <span className="text-[10px] text-slate-400 font-mono">
                    {formatRelative(log.createdAt)}
                  </span>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
