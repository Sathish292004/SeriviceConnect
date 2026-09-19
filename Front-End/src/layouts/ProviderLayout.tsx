import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import {
  LayoutDashboard, User, Settings, Briefcase, Clock, CalendarCheck,
  Image, Menu, X, LogOut, AlertCircle, Compass
} from 'lucide-react'
import { useAuthStore } from '@/store/authStore'
import { Avatar } from '@/components/shared/Avatar'

const SIDEBAR_ITEMS = [
  { to: '/provider/dashboard', icon: LayoutDashboard, label: 'Overview' },
  { to: '/provider/bookings', icon: CalendarCheck, label: 'Job Orders' },
  { to: '/provider/catalog', icon: Briefcase, label: 'Service Offerings' },
  { to: '/provider/availability', icon: Clock, label: 'Working Hours' },
  { to: '/provider/profile', icon: User, label: 'Business Profile' },
  { to: '/provider/photos', icon: Image, label: 'Work Portfolio' },
  { to: '/provider/settings', icon: Settings, label: 'Settings' },
]

export default function ProviderLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const navigate = useNavigate()
  const clearAuth = useAuthStore((s) => s.clearAuth)
  const user = useAuthStore((s) => s.user)

  const handleLogout = () => {
    clearAuth()
    navigate('/provider/login')
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col text-slate-800">
      <a href="#main-content" className="skip-to-content">
        Skip to content
      </a>

      {/* Header */}
      <header className="bg-white border-b border-slate-200/80 sticky top-0 z-40">
        <div className="flex items-center justify-between h-16 px-4 lg:px-8">
          <div className="flex items-center gap-3">
            <button
              className="lg:hidden p-2 rounded-xl text-slate-500 hover:text-slate-900 hover:bg-slate-100"
              onClick={() => setSidebarOpen(!sidebarOpen)}
              aria-label="Toggle sidebar"
            >
              {sidebarOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
            <NavLink to="/provider/dashboard" className="flex items-center gap-2.5">
              <div className="w-8 h-8 bg-gradient-to-tr from-blue-600 to-indigo-600 rounded-xl flex items-center justify-center text-white shadow-sm">
                <Compass className="w-4 h-4" />
              </div>
              <span className="text-base font-extrabold text-slate-900 tracking-tight">
                ServiceConnect
              </span>
              <span className="hidden sm:inline-block px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-indigo-50 text-indigo-700 border border-indigo-100">
                Provider Partner
              </span>
            </NavLink>
          </div>

          <div className="flex items-center gap-3.5">
            <div className="flex items-center gap-2.5">
              <Avatar name={user?.email || 'Provider'} size="sm" />
              <div className="hidden sm:flex flex-col text-left">
                <span className="text-xs font-bold text-slate-800 leading-tight">
                  {user?.email?.split('@')[0]}
                </span>
                <span className="text-[10px] text-slate-400">Partner Account</span>
              </div>
            </div>

            <div className="h-4 w-px bg-slate-200" />

            <button
              onClick={handleLogout}
              className="p-2 rounded-xl text-slate-500 hover:text-rose-600 hover:bg-rose-50 transition-colors flex items-center gap-1.5 text-xs font-semibold"
              title="Sign out of account"
            >
              <LogOut className="w-4 h-4" />
              <span className="hidden sm:inline">Logout</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Container */}
      <div className="flex flex-1">
        {/* Sidebar */}
        <aside
          className={`fixed lg:static inset-y-0 left-0 z-30 w-64 bg-white border-r border-slate-200/80 pt-16 lg:pt-0 transform transition-transform duration-200 ease-in-out ${
            sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
          }`}
          aria-label="Provider navigation"
        >
          <div className="p-4 space-y-1">
            <NavLink
              to="/provider/onboarding"
              onClick={() => setSidebarOpen(false)}
              className="flex items-center gap-2.5 p-3 mb-3 rounded-xl bg-amber-50 border border-amber-200/80 text-amber-900 text-xs font-semibold hover:bg-amber-100/80 transition-colors"
            >
              <AlertCircle className="w-4 h-4 text-amber-600 flex-shrink-0" />
              <span>Partner Onboarding Guide</span>
            </NavLink>

            <p className="px-3 py-1.5 text-[11px] font-bold text-slate-400 uppercase tracking-wider">
              Management
            </p>
            {SIDEBAR_ITEMS.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={() => setSidebarOpen(false)}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs sm:text-sm font-semibold transition-all ${
                    isActive
                      ? 'bg-blue-50 text-blue-600 shadow-sm'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                  }`
                }
              >
                <item.icon className="w-4 h-4 flex-shrink-0" />
                <span>{item.label}</span>
              </NavLink>
            ))}
          </div>
        </aside>

        {/* Backdrop for mobile */}
        {sidebarOpen && (
          <div
            className="fixed inset-0 bg-slate-900/30 backdrop-blur-xs z-20 lg:hidden"
            onClick={() => setSidebarOpen(false)}
            aria-hidden="true"
          />
        )}

        {/* Content Viewport */}
        <main id="main-content" className="flex-1 p-5 sm:p-8 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
