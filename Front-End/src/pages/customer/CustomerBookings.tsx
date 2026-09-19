import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  CalendarCheck, Clock, MapPin, ChevronRight, CreditCard,
  Star, ArrowRight, Filter
} from 'lucide-react'
import { bookingApi } from '@/api/booking'
import { BookingStatusBadge } from '@/components/shared/StatusBadge'
import { Pagination } from '@/components/shared/Pagination'
import { LoadingState, ErrorState, EmptyState } from '@/components/shared/UxStates'
import { formatDate, formatPrice } from '@/utils/formatters'
import type { BookingStatus } from '@/types'

const STATUS_TABS: { label: string; value: BookingStatus | '' }[] = [
  { label: 'All Bookings', value: '' },
  { label: 'Pending', value: 'PENDING' },
  { label: 'Accepted', value: 'ACCEPTED' },
  { label: 'Completed', value: 'COMPLETED' },
  { label: 'Cancelled', value: 'CANCELLED' },
]

export default function CustomerBookings() {
  const [statusFilter, setStatusFilter] = useState<BookingStatus | ''>('')
  const [page, setPage] = useState(0)

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['customer', 'bookings', statusFilter, page],
    queryFn: () => bookingApi.getMyBookings({ page, size: 10 }),
    select: (res) => res.data,
  })

  const allBookings = data?.content ?? []
  const bookings = statusFilter
    ? allBookings.filter((b) => b.status === statusFilter)
    : allBookings

  return (
    <div className="space-y-6 max-w-5xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            My Appointments & Bookings
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Track scheduled home visits, review job summaries, and complete payments.
          </p>
        </div>
        <Link
          to="/customer/providers"
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold shadow-sm transition-all self-start sm:self-auto"
        >
          <span>Book New Service</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </Link>
      </div>

      {/* Status Filter Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 no-scrollbar">
        {STATUS_TABS.map((tab) => {
          const isSelected = statusFilter === tab.value
          return (
            <button
              key={tab.value}
              onClick={() => {
                setStatusFilter(tab.value)
                setPage(0)
              }}
              className={`px-4 py-2 rounded-xl text-xs sm:text-sm font-semibold whitespace-nowrap transition-all ${
                isSelected
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'
              }`}
            >
              {tab.label}
            </button>
          )
        })}
      </div>

      {/* Bookings List */}
      {isLoading ? (
        <LoadingState message="Loading your appointments..." />
      ) : error ? (
        <ErrorState
          message="Failed to retrieve bookings."
          action={{ label: 'Retry', onClick: () => refetch() }}
        />
      ) : bookings.length === 0 ? (
        <EmptyState
          title={statusFilter ? `No ${statusFilter.toLowerCase()} bookings found` : 'No appointments scheduled'}
          message={
            statusFilter
              ? 'You do not have any bookings matching this status filter.'
              : 'You have not made any bookings yet. Choose a certified provider to schedule a visit.'
          }
          action={{
            label: statusFilter ? 'Clear Filter' : 'Find Providers',
            onClick: () => {
              if (statusFilter) setStatusFilter('')
              else window.location.assign('/customer/providers')
            },
          }}
        />
      ) : (
        <div className="space-y-3.5">
          {bookings.map((booking) => (
            <div
              key={booking.id}
              className="bg-white rounded-2xl p-5 border border-slate-200/80 hover:shadow-md transition-all flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4"
            >
              <div className="flex items-start gap-4">
                <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <CalendarCheck className="w-6 h-6" />
                </div>
                <div className="space-y-1">
                  <div className="flex items-center gap-2.5 flex-wrap">
                    <h3 className="text-base font-bold text-slate-900">
                      Booking #{booking.id}
                    </h3>
                    <BookingStatusBadge status={booking.status} />
                  </div>

                  <p className="text-xs text-slate-500 font-medium flex items-center gap-1.5">
                    <Clock className="w-3.5 h-3.5 text-slate-400" />
                    <span>Scheduled for {formatDate(booking.requestedStartAt)}</span>
                  </p>

                  {booking.serviceAddress && (
                    <p className="text-xs text-slate-500 flex items-center gap-1.5">
                      <MapPin className="w-3.5 h-3.5 text-slate-400" />
                      <span className="truncate max-w-sm">{booking.serviceAddress}</span>
                    </p>
                  )}
                </div>
              </div>

              <div className="flex sm:flex-col items-center sm:items-end justify-between w-full sm:w-auto pt-3 sm:pt-0 border-t sm:border-t-0 border-slate-100 gap-2">
                <span className="text-lg font-black text-slate-900">
                  {formatPrice(booking.priceSnapshot)}
                </span>

                <div className="flex items-center gap-2">
                  {booking.status === 'ACCEPTED' && (
                    <Link
                      to={`/customer/payment/${booking.id}`}
                      className="px-3.5 py-1.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold shadow-sm flex items-center gap-1"
                    >
                      <CreditCard className="w-3 h-3" /> Pay
                    </Link>
                  )}
                  {booking.status === 'COMPLETED' && (
                    <Link
                      to={`/customer/reviews?bookingId=${booking.id}`}
                      className="px-3.5 py-1.5 rounded-lg bg-amber-50 hover:bg-amber-100 text-amber-800 border border-amber-200 text-xs font-semibold flex items-center gap-1"
                    >
                      <Star className="w-3 h-3 text-amber-500 fill-amber-500" /> Review
                    </Link>
                  )}
                  <Link
                    to={`/customer/bookings/${booking.id}`}
                    className="px-3.5 py-1.5 rounded-lg bg-slate-50 hover:bg-slate-100 text-slate-700 border border-slate-200 text-xs font-semibold flex items-center gap-1"
                  >
                    <span>Details</span>
                    <ChevronRight className="w-3.5 h-3.5" />
                  </Link>
                </div>
              </div>
            </div>
          ))}

          {data && data.totalPages > 1 && (
            <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />
          )}
        </div>
      )}
    </div>
  )
}
