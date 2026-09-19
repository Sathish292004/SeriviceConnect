import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import {
  LayoutDashboard, Users, Headphones, BookOpen, ClipboardList,
  Menu, X, LogOut, ShieldCheck, UserCheck,
} from 'lucide-react'
import { useAuthStore, selectRole } from '@/store/authStore'

const ADMIN_ITEMS = [
  { to: '/admin/dashboard', icon: LayoutDashboard, label: 'Dashboard', roles: ['ADMIN'] },
  { to: '/admin/customers', icon: UserCheck, label: 'Customers', roles: ['ADMIN'] },
  { to: '/admin/providers', icon: Users, label: 'Providers', roles: ['ADMIN'] },
  { to: '/admin/tickets', icon: Headphones, label: 'Tickets', roles: ['ADMIN'] },
  { to: '/admin/help-center', icon: BookOpen, label: 'Help Center', roles: ['ADMIN'] },
  { to: '/admin/audit-logs', icon: ClipboardList, label: 'Audit Logs', roles: ['ADMIN'] },
]

const SUPPORT_ITEMS = [
  { to: '/support/tickets', icon: Headphones, label: 'My Tickets', roles: ['SUPPORT_AGENT'] },
]

export default function AdminLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const navigate = useNavigate()
  const clearAuth = useAuthStore((s) => s.clearAuth)
  const user = useAuthStore((s) => s.user)
  const role = useAuthStore(selectRole)

  const handleLogout = () => {
    clearAuth()
    navigate('/admin/login')
  }

  const visibleAdminItems = ADMIN_ITEMS.filter((i) => role && i.roles.includes(role))
  const visibleSupportItems = SUPPORT_ITEMS.filter((i) => role && i.roles.includes(role))

  return (
    <div className="min-h-screen bg-[#F1F5F9]">
      <a href="#main-content" className="skip-to-content">Skip to content</a>

      <header className="bg-white border-b border-[#E2E8F0] sticky top-0 z-40">
        <div className="flex items-center justify-between h-14 px-4 lg:px-6">
          <div className="flex items-center gap-3">
            <button className="lg:hidden p-1.5 text-[#64748B]" onClick={() => setSidebarOpen(!sidebarOpen)} aria-label="Toggle sidebar">
              {sidebarOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
            <NavLink to={role === 'ADMIN' ? '/admin/dashboard' : '/support/tickets'} className="flex items-center gap-2">
              <div className="w-7 h-7 bg-[#2563EB] rounded-lg flex items-center justify-center">
                <ShieldCheck className="w-4 h-4 text-white" />
              </div>
              <span className="text-base font-semibold text-[#0F172A]">ServiceConnect</span>
              <span className="text-xs bg-[#EDE9FE] text-[#7C3AED] px-2 py-0.5 rounded-full font-medium">
                {role === 'ADMIN' ? 'Admin' : 'Support'}
              </span>
            </NavLink>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-sm text-[#64748B] hidden sm:block">{user?.email}</span>
            <button onClick={handleLogout} className="sc-btn-ghost text-xs gap-1.5">
              <LogOut className="w-4 h-4" /> Logout
            </button>
          </div>
        </div>
      </header>

      <div className="flex">
        <aside
          className={`fixed lg:static inset-y-0 left-0 z-30 w-64 bg-white border-r border-[#E2E8F0] pt-16 lg:pt-0 transform transition-transform duration-200 ${
            sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
          }`}
          aria-label="Admin navigation"
        >
          <nav className="p-4 space-y-1">
            {visibleAdminItems.length > 0 && (
              <>
                <p className="px-4 py-1 text-xs font-semibold text-[#94A3B8] uppercase tracking-wider">Administration</p>
                {visibleAdminItems.map((item) => (
                  <NavLink key={item.to} to={item.to} onClick={() => setSidebarOpen(false)} className={({ isActive }) => isActive ? 'sidebar-item-active' : 'sidebar-item'}>
                    <item.icon className="w-5 h-5 flex-shrink-0" />
                    {item.label}
                  </NavLink>
                ))}
              </>
            )}
            {visibleSupportItems.length > 0 && (
              <>
                {visibleSupportItems.map((item) => (
                  <NavLink key={item.to} to={item.to} onClick={() => setSidebarOpen(false)} className={({ isActive }) => isActive ? 'sidebar-item-active' : 'sidebar-item'}>
                    <item.icon className="w-5 h-5 flex-shrink-0" />
                    {item.label}
                  </NavLink>
                ))}
              </>
            )}
          </nav>
        </aside>

        {sidebarOpen && <div className="fixed inset-0 bg-black/20 z-20 lg:hidden" onClick={() => setSidebarOpen(false)} />}

        <main id="main-content" className="flex-1 min-h-[calc(100vh-3.5rem)] p-4 lg:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
