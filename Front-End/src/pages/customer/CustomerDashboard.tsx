import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import {
  CalendarCheck, Clock, Star, Headphones, ArrowRight, MapPin, Search,
  CheckCircle2, AlertCircle, CreditCard, ChevronRight, Zap
} from 'lucide-react'
import { bookingApi } from '@/api/booking'
import { useAuthStore } from '@/store/authStore'
import { BookingStatusBadge } from '@/components/shared/StatusBadge'
import { LoadingState, ErrorState } from '@/components/shared/UxStates'
import { formatDate, formatPrice } from '@/utils/formatters'

export default function CustomerDashboard() {
  const user = useAuthStore((s) => s.user)

  const { data: bookingsData, isLoading, error, refetch } = useQuery({
    queryKey: ['customer', 'bookings', 'dashboard'],
    queryFn: () => bookingApi.getMyBookings({ page: 0, size: 5 }),
    select: (res) => res.data,
  })

  const bookings = bookingsData?.content ?? []
  const totalBookings = bookingsData?.totalElements ?? bookings.length
  const pendingCount = bookings.filter((b) => b.status === 'PENDING').length
  const acceptedCount = bookings.filter((b) => b.status === 'ACCEPTED').length
  const completedCount = bookings.filter((b) => b.status === 'COMPLETED').length

  // Find the next upcoming booking (ACCEPTED or PENDING)
  const activeBooking = bookings.find((b) => b.status === 'ACCEPTED' || b.status === 'PENDING')

  return (
    <div className="space-y-8 max-w-6xl">
      {/* Welcome Banner */}
      <div className="bg-gradient-to-r from-blue-700 via-indigo-700 to-blue-800 rounded-3xl p-6 sm:p-8 text-white shadow-lg relative overflow-hidden">
        <div className="relative z-10 max-w-2xl space-y-2">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 backdrop-blur-md text-xs font-semibold text-blue-100 border border-white/15">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            Customer Portal
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
            Welcome back, {user?.email?.split('@')[0] || 'Customer'}!
          </h1>
          <p className="text-blue-100 text-sm sm:text-base leading-relaxed">
            Manage your service appointments, track ongoing jobs, and book top-rated professionals.
          </p>
        </div>
      </div>

      {/* KPI Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center flex-shrink-0">
            <CalendarCheck className="w-6 h-6" />
          </div>
          <div>
            <p className="text-2xl font-black text-slate-900">{totalBookings}</p>
            <p className="text-xs font-medium text-slate-500">Total Bookings</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center flex-shrink-0">
            <Clock className="w-6 h-6" />
          </div>
          <div>
            <p className="text-2xl font-black text-slate-900">{pendingCount}</p>
            <p className="text-xs font-medium text-slate-500">Pending Confirmation</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center flex-shrink-0">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <div>
            <p className="text-2xl font-black text-slate-900">{acceptedCount}</p>
            <p className="text-xs font-medium text-slate-500">Confirmed Jobs</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center flex-shrink-0">
            <Star className="w-6 h-6" />
          </div>
          <div>
            <p className="text-2xl font-black text-slate-900">{completedCount}</p>
            <p className="text-xs font-medium text-slate-500">Completed Jobs</p>
          </div>
        </div>
      </div>

      {/* Active Booking Banner if present */}
      {activeBooking && (
        <div className="bg-blue-50/80 border border-blue-200/80 rounded-2xl p-5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="flex items-start gap-3.5">
            <div className="w-10 h-10 rounded-xl bg-blue-600 text-white flex items-center justify-center flex-shrink-0 mt-0.5">
              <Zap className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold uppercase tracking-wider text-blue-700">Upcoming Service</span>
                <BookingStatusBadge status={activeBooking.status} />
              </div>
              <h3 className="text-base font-bold text-slate-900 mt-0.5">
                Booking #{activeBooking.id} · Scheduled for {formatDate(activeBooking.requestedStartAt)}
              </h3>
              <p className="text-xs text-slate-600 mt-0.5">
                {activeBooking.serviceAddress || 'Doorstep service'} · {formatPrice(activeBooking.priceSnapshot)}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2.5 self-end sm:self-center">
            {activeBooking.status === 'ACCEPTED' && (
              <Link
                to={`/customer/payment/${activeBooking.id}`}
                className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold shadow-sm transition-all"
              >
                <CreditCard className="w-3.5 h-3.5" />
                <span>Pay Now</span>
              </Link>
            )}
            <Link
              to={`/customer/bookings/${activeBooking.id}`}
              className="inline-flex items-center gap-1 px-4 py-2 rounded-xl bg-white border border-slate-200 text-slate-700 text-xs font-semibold hover:bg-slate-50 transition-all"
            >
              <span>View Details</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </div>
      )}

      {/* Quick Discovery Actions */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <Link
          to="/customer/providers"
          className="bg-white rounded-2xl p-6 border border-slate-200/80 hover:border-blue-400 hover:shadow-md transition-all flex items-center justify-between group"
        >
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center group-hover:scale-110 transition-transform">
              <MapPin className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900 group-hover:text-blue-600 transition-colors">
                Discover Nearby Providers
              </h3>
              <p className="text-xs text-slate-500 mt-0.5">Browse interactive map and compare certified pros</p>
            </div>
          </div>
          <ChevronRight className="w-5 h-5 text-slate-400 group-hover:translate-x-1 transition-transform" />
        </Link>

        <Link
          to="/customer/services"
          className="bg-white rounded-2xl p-6 border border-slate-200/80 hover:border-blue-400 hover:shadow-md transition-all flex items-center justify-between group"
        >
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center group-hover:scale-110 transition-transform">
              <Search className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900 group-hover:text-indigo-600 transition-colors">
                Browse Service Catalog
              </h3>
              <p className="text-xs text-slate-500 mt-0.5">Explore fixed-rate electrical, AC, and plumbing tasks</p>
            </div>
          </div>
          <ChevronRight className="w-5 h-5 text-slate-400 group-hover:translate-x-1 transition-transform" />
        </Link>
      </div>

      {/* Recent Bookings List */}
      <div className="bg-white rounded-3xl border border-slate-200/80 shadow-sm overflow-hidden">
        <div className="p-6 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h2 className="text-lg font-bold text-slate-900">Recent Appointments</h2>
            <p className="text-xs text-slate-500 mt-0.5">Your most recent service requests and bookings</p>
          </div>
          <Link
            to="/customer/bookings"
            className="text-xs font-bold text-blue-600 hover:text-blue-700 flex items-center gap-1"
          >
            <span>View All</span>
            <ChevronRight className="w-4 h-4" />
          </Link>
        </div>

        {isLoading ? (
          <div className="p-8"><LoadingState message="Loading recent bookings..." /></div>
        ) : error ? (
          <div className="p-8"><ErrorState message="Could not fetch bookings." action={{ label: 'Retry', onClick: () => refetch() }} /></div>
        ) : bookings.length === 0 ? (
          <div className="p-12 text-center space-y-3">
            <div className="w-12 h-12 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center mx-auto">
              <CalendarCheck className="w-6 h-6" />
            </div>
            <h3 className="text-base font-bold text-slate-800">No bookings yet</h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              You haven't requested any services yet. Explore verified providers in your area to book your first service.
            </p>
            <Link
              to="/customer/providers"
              className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-blue-600 text-white text-xs font-semibold shadow-sm mt-2"
            >
              <span>Explore Providers</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {bookings.map((booking) => (
              <Link
                key={booking.id}
                to={`/customer/bookings/${booking.id}`}
                className="p-5 flex items-center justify-between hover:bg-slate-50/80 transition-colors group"
              >
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 rounded-xl bg-slate-100 text-slate-600 flex items-center justify-center flex-shrink-0 group-hover:bg-blue-50 group-hover:text-blue-600 transition-colors">
                    <CalendarCheck className="w-5 h-5" />
                  </div>
                  <div>
                    <h4 className="text-sm font-bold text-slate-900 group-hover:text-blue-600 transition-colors">
                      Booking #{booking.id}
                    </h4>
                    <p className="text-xs text-slate-500 mt-0.5">
                      {formatDate(booking.requestedStartAt)} · {formatPrice(booking.priceSnapshot)}
                    </p>
                    {booking.serviceAddress && (
                      <p className="text-[11px] text-slate-400 mt-0.5 truncate max-w-md">
                        {booking.serviceAddress}
                      </p>
                    )}
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <BookingStatusBadge status={booking.status} />
                  <ChevronRight className="w-4 h-4 text-slate-300 group-hover:text-slate-600 group-hover:translate-x-0.5 transition-all" />
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
