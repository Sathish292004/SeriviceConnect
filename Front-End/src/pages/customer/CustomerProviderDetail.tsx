import { useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  ArrowLeft, MapPin, Clock, Phone, Mail, Calendar,
  CheckCircle2, X
} from 'lucide-react'
import { providerDiscoveryApi } from '@/api/provider'
import { catalogApi } from '@/api/catalog'
import { reviewApi } from '@/api/review'
import { bookingApi } from '@/api/booking'
import { ProviderStatusBadge } from '@/components/shared/StatusBadge'
import { StarRating } from '@/components/shared/StarRating'
import { Pagination } from '@/components/shared/Pagination'
import { LoadingState, ErrorState } from '@/components/shared/UxStates'
import { formatPrice, formatDate, formatTime } from '@/utils/formatters'
import type { CatalogItem } from '@/types'

export default function CustomerProviderDetail() {
  const { id } = useParams<{ id: string }>()
  const providerId = Number(id)
  const navigate = useNavigate()
  const qc = useQueryClient()

  const [reviewPage, setReviewPage] = useState(0)
  const [selectedService, setSelectedService] = useState<CatalogItem | null>(null)
  const [bookingModalOpen, setBookingModalOpen] = useState(false)

  // Booking form state
  const [bookingDate, setBookingDate] = useState('')
  const [bookingTime, setBookingTime] = useState('10:00')
  const [serviceAddress, setServiceAddress] = useState('')
  const [description, setDescription] = useState('')

  const { data: provider, isLoading, error } = useQuery({
    queryKey: ['provider', 'public', providerId],
    queryFn: () => providerDiscoveryApi.getPublic(providerId),
    select: (r) => r.data,
    enabled: !isNaN(providerId),
  })

  const { data: catalog } = useQuery({
    queryKey: ['catalog', 'provider', providerId],
    queryFn: () => catalogApi.getByProvider(providerId, { size: 20 }),
    select: (r) => r.data,
    enabled: !isNaN(providerId),
  })

  const { data: availability } = useQuery({
    queryKey: ['provider', 'availability', providerId],
    queryFn: () => providerDiscoveryApi.getActiveAvailability(providerId),
    select: (r) => r.data,
    enabled: !isNaN(providerId),
  })

  const { data: photos } = useQuery({
    queryKey: ['provider', 'photos', providerId],
    queryFn: () => providerDiscoveryApi.getPhotos(providerId),
    select: (r) => r.data,
    enabled: !isNaN(providerId),
  })

  const { data: reviews } = useQuery({
    queryKey: ['reviews', 'provider', providerId, reviewPage],
    queryFn: () => reviewApi.getByProvider(providerId, { page: reviewPage, size: 5 }),
    select: (r) => r.data,
    enabled: !isNaN(providerId),
  })

  const bookMutation = useMutation({
    mutationFn: async () => {
      if (!selectedService) throw new Error('Please select a service')
      if (!bookingDate) throw new Error('Please select a booking date')
      if (!serviceAddress.trim()) throw new Error('Please enter the service address')

      const timeStr = bookingTime ? (bookingTime.length === 5 ? `${bookingTime}:00` : bookingTime) : '10:00:00'
      const requestedStartAt = `${bookingDate}T${timeStr}Z`

      return bookingApi.create({
        providerId,
        catalogItemId: selectedService.id,
        description: description.trim() || `Service request for ${selectedService.name}`,
        serviceAddress: serviceAddress.trim(),
        latitude: 12.9716,
        longitude: 77.5946,
        requestedStartAt,
      })
    },
    onSuccess: (res) => {
      toast.success('Booking requested successfully!')
      qc.invalidateQueries({ queryKey: ['customer', 'bookings'] })
      setBookingModalOpen(false)
      navigate(`/customer/bookings/${res.data.id}`)
    },
    onError: (err: unknown) => {
      const msg = err instanceof Error ? err.message : 'Failed to create booking request'
      toast.error(msg)
    },
  })

  if (isLoading) {
    return (
      <div className="py-8">
        <LoadingState message="Loading provider details…" />
      </div>
    )
  }

  if (error || !provider) {
    return (
      <div className="py-8">
        <ErrorState message="Provider not found." action={{ label: 'Back to Providers', onClick: () => navigate('/customer/providers') }} />
      </div>
    )
  }

  const services = (catalog?.content ?? []).filter((s) => s.active)
  const reviewList = reviews?.content ?? []
  const activeAvailability = (availability ?? []).filter((a) => a.active)

  const handleOpenBooking = (service?: CatalogItem) => {
    if (service) {
      setSelectedService(service)
    } else if (services.length > 0 && !selectedService) {
      setSelectedService(services[0])
    }
    // Default date to tomorrow if empty
    if (!bookingDate) {
      const tomorrow = new Date()
      tomorrow.setDate(tomorrow.getDate() + 1)
      setBookingDate(tomorrow.toISOString().split('T')[0])
    }
    setBookingModalOpen(true)
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <Link
        to="/customer/providers"
        className="inline-flex items-center gap-1 text-sm text-[#64748B] hover:text-[#2563EB]"
      >
        <ArrowLeft className="w-4 h-4" /> Back to Providers
      </Link>

      {/* Provider Header Card */}
      <div className="sc-card p-6">
        <div className="flex flex-col sm:flex-row items-start justify-between gap-4">
          <div className="flex items-start gap-4">
            <div className="w-16 h-16 rounded-full bg-[#EFF6FF] flex items-center justify-center text-2xl font-bold text-[#2563EB] flex-shrink-0">
              {provider.businessName.charAt(0)}
            </div>
            <div>
              <div className="flex items-center gap-3 flex-wrap">
                <h1 className="text-xl font-bold text-[#0F172A]">{provider.businessName}</h1>
                <ProviderStatusBadge status={provider.status} />
              </div>
              {provider.description && (
                <p className="text-sm text-[#64748B] mt-1">{provider.description}</p>
              )}
              <div className="flex items-center gap-4 mt-2 text-sm text-[#64748B] flex-wrap">
                {provider.city && (
                  <span className="flex items-center gap-1">
                    <MapPin className="w-3.5 h-3.5" /> {provider.city}{provider.state ? `, ${provider.state}` : ''}
                  </span>
                )}
                {provider.phone && (
                  <span className="flex items-center gap-1">
                    <Phone className="w-3.5 h-3.5" /> {provider.phone}
                  </span>
                )}
                {provider.email && (
                  <span className="flex items-center gap-1">
                    <Mail className="w-3.5 h-3.5" /> {provider.email}
                  </span>
                )}
              </div>
            </div>
          </div>
          <button
            onClick={() => handleOpenBooking()}
            disabled={services.length === 0}
            className="sc-btn-primary text-sm px-6 py-2.5 flex-shrink-0"
          >
            Book Appointment
          </button>
        </div>
      </div>

      {/* Portfolio Photos */}
      {photos && photos.length > 0 && (
        <div className="sc-card p-5">
          <h2 className="text-base font-semibold text-[#0F172A] mb-3">Portfolio & Past Work</h2>
          <div className="flex gap-3 overflow-x-auto no-scrollbar pb-1">
            {photos.map((photo) => (
              <img
                key={photo.id}
                src={photo.imageUrl}
                alt="Provider portfolio"
                className="w-48 h-32 rounded-[8px] object-cover flex-shrink-0 border border-[#E2E8F0]"
              />
            ))}
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Services & Reviews Column */}
        <div className="lg:col-span-2 space-y-6">
          {/* Services List */}
          <div className="sc-card p-5">
            <h2 className="text-base font-semibold text-[#0F172A] mb-3">Available Services</h2>
            {services.length === 0 ? (
              <p className="text-sm text-[#64748B] py-4">No active services currently listed.</p>
            ) : (
              <div className="divide-y divide-[#E2E8F0]">
                {services.map((item) => (
                  <div key={item.id} className="py-3.5 flex items-center justify-between gap-4">
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <p className="text-sm font-semibold text-[#0F172A]">{item.name}</p>
                        <span className="tag-pill">{item.category}</span>
                      </div>
                      {item.description && (
                        <p className="text-xs text-[#64748B] mt-0.5">{item.description}</p>
                      )}
                      {item.durationMinutes && (
                        <p className="text-xs text-[#94A3B8] mt-1">Est. Duration: {item.durationMinutes} mins</p>
                      )}
                    </div>
                    <div className="text-right flex-shrink-0 flex flex-col items-end gap-1.5">
                      <p className="text-base font-bold text-[#0F172A]">{formatPrice(item.price)}</p>
                      <button
                        onClick={() => handleOpenBooking(item)}
                        className="sc-btn-outline text-xs px-3 py-1"
                      >
                        Book This
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Customer Reviews */}
          <div className="sc-card p-5">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-base font-semibold text-[#0F172A]">Customer Reviews</h2>
              <span className="text-xs text-[#64748B]">
                {reviews?.totalElements ?? 0} total review{(reviews?.totalElements ?? 0) !== 1 ? 's' : ''}
              </span>
            </div>
            {reviewList.length === 0 ? (
              <p className="text-sm text-[#64748B] py-4">No reviews yet for this provider.</p>
            ) : (
              <>
                <div className="space-y-4">
                  {reviewList.map((r) => (
                    <div key={r.id} className="border-b border-[#E2E8F0] pb-3.5 last:border-0">
                      <div className="flex items-center justify-between mb-1">
                        <StarRating value={r.rating} readonly size="sm" />
                        <span className="text-xs text-[#94A3B8]">{formatDate(r.createdAt)}</span>
                      </div>
                      {r.comment && <p className="text-sm text-[#0F172A] mt-1">{r.comment}</p>}
                    </div>
                  ))}
                </div>
                <Pagination page={reviewPage} totalPages={reviews?.totalPages ?? 1} onPageChange={setReviewPage} />
              </>
            )}
          </div>
        </div>

        {/* Schedule & Availability Sidebar */}
        <div className="space-y-6">
          <div className="sc-card p-5">
            <h2 className="text-base font-semibold text-[#0F172A] mb-3 flex items-center gap-2">
              <Clock className="w-4 h-4 text-[#16A34A]" /> Working Hours
            </h2>
            {activeAvailability.length === 0 ? (
              <p className="text-sm text-[#64748B]">Schedule not specified.</p>
            ) : (
              <div className="space-y-2">
                {activeAvailability.map((slot) => (
                  <div key={slot.id} className="flex items-center justify-between text-sm py-1 border-b border-[#F1F5F9] last:border-0">
                    <span className="font-medium text-[#0F172A]">
                      {slot.dayOfWeek.charAt(0) + slot.dayOfWeek.slice(1).toLowerCase()}
                    </span>
                    <span className="text-[#64748B]">
                      {formatTime(slot.startTime)} – {formatTime(slot.endTime)}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="sc-card p-5 bg-[#EFF6FF] border-[#BFDBFE]">
            <h3 className="text-sm font-semibold text-[#1E40AF] flex items-center gap-1.5 mb-1.5">
              <CheckCircle2 className="w-4 h-4 text-[#2563EB]" /> Service Guarantee
            </h3>
            <p className="text-xs text-[#1E3A8A] leading-relaxed">
              All bookings are protected under ServiceConnect. Verified provider credentials and secure payment releases.
            </p>
          </div>
        </div>
      </div>

      {/* Booking Modal */}
      {bookingModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
          <div className="bg-white rounded-card shadow-lg max-w-lg w-full p-6 space-y-4 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-[#E2E8F0] pb-3">
              <div className="flex items-center gap-2">
                <Calendar className="w-5 h-5 text-[#2563EB]" />
                <h2 className="text-lg font-bold text-[#0F172A]">Book Service Appointment</h2>
              </div>
              <button
                onClick={() => setBookingModalOpen(false)}
                className="sc-btn-ghost p-1 text-[#64748B]"
                aria-label="Close"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Service Selection */}
            <div>
              <label className="form-label">Selected Service</label>
              <select
                value={selectedService?.id ?? ''}
                onChange={(e) => {
                  const item = services.find((s) => s.id === Number(e.target.value))
                  if (item) setSelectedService(item)
                }}
                className="sc-input"
              >
                {services.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name} — {formatPrice(s.price)}
                  </option>
                ))}
              </select>
            </div>

            {/* Date & Time Selection */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="form-label">Date</label>
                <input
                  type="date"
                  value={bookingDate}
                  min={new Date().toISOString().split('T')[0]}
                  onChange={(e) => setBookingDate(e.target.value)}
                  className="sc-input"
                />
              </div>
              <div>
                <label className="form-label">Time</label>
                <input
                  type="time"
                  value={bookingTime}
                  onChange={(e) => setBookingTime(e.target.value)}
                  className="sc-input"
                />
              </div>
            </div>

            {/* Service Address */}
            <div>
              <label className="form-label">Service Address *</label>
              <input
                value={serviceAddress}
                onChange={(e) => setServiceAddress(e.target.value)}
                placeholder="Street address, apartment, locality"
                className="sc-input"
              />
            </div>

            {/* Notes / Description */}
            <div>
              <label className="form-label">Description / Specific Requests (optional)</label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Explain the service requirements or issues you are experiencing…"
                className="sc-input min-h-[70px]"
              />
            </div>

            {/* Price Summary */}
            {selectedService && (
              <div className="bg-[#F8FAFC] border border-[#E2E8F0] p-3 rounded-[8px] flex items-center justify-between text-sm">
                <span className="text-[#64748B]">Total Amount Payable:</span>
                <span className="text-base font-bold text-[#0F172A]">{formatPrice(selectedService.price)}</span>
              </div>
            )}

            <div className="flex items-center gap-2 pt-2">
              <button
                type="button"
                onClick={() => setBookingModalOpen(false)}
                className="sc-btn-outline flex-1 text-sm"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => bookMutation.mutate()}
                disabled={bookMutation.isPending || !selectedService || !serviceAddress.trim() || !bookingDate}
                className="sc-btn-primary flex-1 text-sm"
              >
                {bookMutation.isPending ? 'Submitting…' : 'Confirm Booking'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
