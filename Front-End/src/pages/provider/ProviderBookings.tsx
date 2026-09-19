import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  CalendarCheck, Clock, MapPin, Check, X, CheckCircle2,
  ChevronRight, Filter
} from 'lucide-react'
import { bookingApi } from '@/api/booking'
import { BookingStatusBadge } from '@/components/shared/StatusBadge'
import { Pagination } from '@/components/shared/Pagination'
import { LoadingState, ErrorState, EmptyState } from '@/components/shared/UxStates'
import { formatDate, formatPrice } from '@/utils/formatters'
import type { BookingStatus } from '@/types'

const STATUS_TABS: { label: string; value: BookingStatus | '' }[] = [
  { label: 'All Jobs', value: '' },
  { label: 'Pending Requests', value: 'PENDING' },
  { label: 'Confirmed Jobs', value: 'ACCEPTED' },
  { label: 'Completed Jobs', value: 'COMPLETED' },
  { label: 'Cancelled', value: 'CANCELLED' },
]

export default function ProviderBookings() {
  const qc = useQueryClient()
  const [statusFilter, setStatusFilter] = useState<BookingStatus | ''>('')
  const [page, setPage] = useState(0)

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['provider', 'bookings', statusFilter, page],
    queryFn: () => bookingApi.getProviderBookings({ page, size: 10 }),
    select: (r) => r.data,
  })

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: 'ACCEPTED' | 'REJECTED' | 'COMPLETED' }) =>
      bookingApi.updateStatus(id, { status }),
    onSuccess: (_, vars) => {
      toast.success(`Booking #${vars.id} marked as ${vars.status.toLowerCase()}.`)
      qc.invalidateQueries({ queryKey: ['provider', 'bookings'] })
    },
    onError: () => toast.error('Failed to update booking status.'),
  })

  const allBookings = data?.content ?? []
  const bookings = statusFilter
    ? allBookings.filter((b) => b.status === statusFilter)
    : allBookings

  return (
    <div className="space-y-6 max-w-5xl">
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
          Client Bookings & Work Orders
        </h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Review appointment requests, manage your schedule, and mark completed jobs.
        </p>
      </div>

      {/* Tabs */}
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
        <LoadingState message="Loading work orders..." />
      ) : error ? (
        <ErrorState
          message="Failed to retrieve bookings."
          action={{ label: 'Retry', onClick: () => refetch() }}
        />
      ) : bookings.length === 0 ? (
        <EmptyState
          title={statusFilter ? `No ${statusFilter.toLowerCase()} bookings` : 'No bookings found'}
          message={
            statusFilter
              ? 'No work orders currently match this filter.'
              : 'You do not have any appointments yet. Ensure your services and working hours are active.'
          }
        />
      ) : (
        <div className="space-y-3.5">
          {bookings.map((booking) => (
            <div
              key={booking.id}
              className="bg-white rounded-2xl p-5 border border-slate-200/80 hover:shadow-md transition-all flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4"
            >
              <div className="space-y-1">
                <div className="flex items-center gap-2.5 flex-wrap">
                  <h3 className="text-base font-bold text-slate-900">
                    Booking #{booking.id}
                  </h3>
                  <BookingStatusBadge status={booking.status} />
                </div>

                <p className="text-xs text-slate-500 font-medium flex items-center gap-1.5">
                  <Clock className="w-3.5 h-3.5 text-slate-400" />
                  <span>Scheduled: {formatDate(booking.requestedStartAt)}</span>
                </p>

                {booking.serviceAddress && (
                  <p className="text-xs text-slate-500 flex items-center gap-1.5">
                    <MapPin className="w-3.5 h-3.5 text-slate-400" />
                    <span className="truncate max-w-md">{booking.serviceAddress}</span>
                  </p>
                )}
              </div>

              <div className="flex sm:flex-col items-center sm:items-end justify-between w-full sm:w-auto pt-3 sm:pt-0 border-t sm:border-t-0 border-slate-100 gap-2">
                <span className="text-lg font-black text-slate-900">
                  {formatPrice(booking.priceSnapshot)}
                </span>

                <div className="flex items-center gap-2">
                  {booking.status === 'PENDING' && (
                    <>
                      <button
                        onClick={() =>
                          updateStatusMutation.mutate({ id: booking.id, status: 'ACCEPTED' })
                        }
                        disabled={updateStatusMutation.isPending}
                        className="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold shadow-sm flex items-center gap-1 transition-all"
                      >
                        <Check className="w-3.5 h-3.5" /> Accept
                      </button>
                      <button
                        onClick={() =>
                          updateStatusMutation.mutate({ id: booking.id, status: 'REJECTED' })
                        }
                        disabled={updateStatusMutation.isPending}
                        className="px-3 py-1.5 rounded-lg border border-rose-200 text-rose-600 hover:bg-rose-50 text-xs font-semibold transition-all"
                      >
                        <X className="w-3.5 h-3.5" /> Decline
                      </button>
                    </>
                  )}

                  {booking.status === 'ACCEPTED' && (
                    <button
                      onClick={() =>
                        updateStatusMutation.mutate({ id: booking.id, status: 'COMPLETED' })
                      }
                      disabled={updateStatusMutation.isPending}
                      className="px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold shadow-sm flex items-center gap-1 transition-all"
                    >
                      <CheckCircle2 className="w-3.5 h-3.5" /> Mark Completed
                    </button>
                  )}

                  <Link
                    to={`/provider/bookings/${booking.id}`}
                    className="px-3 py-1.5 rounded-lg bg-slate-50 hover:bg-slate-100 text-slate-700 border border-slate-200 text-xs font-semibold flex items-center gap-1"
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
