import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, MapPin, Clock, Phone, Mail } from 'lucide-react'
import { providerDiscoveryApi } from '@/api/provider'
import { catalogApi } from '@/api/catalog'
import { reviewApi } from '@/api/review'
import { ProviderStatusBadge } from '@/components/shared/StatusBadge'
import { StarRating } from '@/components/shared/StarRating'
import { Pagination } from '@/components/shared/Pagination'
import { LoadingState, ErrorState } from '@/components/shared/UxStates'
import { formatPrice, formatDate, formatTime } from '@/utils/formatters'
import { useState } from 'react'

export default function ProviderPublicProfile() {
  const { id } = useParams<{ id: string }>()
  const providerId = Number(id)
  const [reviewPage, setReviewPage] = useState(0)

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

  if (isLoading) return <div className="page-container py-8"><LoadingState message="Loading provider…" /></div>
  if (error || !provider) return <div className="page-container py-8"><ErrorState message="Provider not found." /></div>

  const services = catalog?.content ?? []
  const reviewList = reviews?.content ?? []

  return (
    <div className="page-container py-6 space-y-6">
      <Link to="/providers" className="inline-flex items-center gap-1 text-sm text-[#64748B] hover:text-[#2563EB]">
        <ArrowLeft className="w-4 h-4" /> Back to Providers
      </Link>

      {/* Header */}
      <div className="sc-card p-6">
        <div className="flex flex-col sm:flex-row items-start gap-4">
          <div className="w-16 h-16 rounded-full bg-[#EFF6FF] flex items-center justify-center text-2xl font-bold text-[#2563EB] flex-shrink-0">
            {provider.businessName.charAt(0)}
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-3 flex-wrap">
              <h1 className="text-xl font-bold text-[#0F172A]">{provider.businessName}</h1>
              <ProviderStatusBadge status={provider.status} />
            </div>
            {provider.description && <p className="text-sm text-[#64748B] mt-1">{provider.description}</p>}
            <div className="flex items-center gap-4 mt-2 text-sm text-[#64748B]">
              {provider.city && <span className="flex items-center gap-1"><MapPin className="w-3.5 h-3.5" /> {provider.city}{provider.state ? `, ${provider.state}` : ''}</span>}
              {provider.phone && <span className="flex items-center gap-1"><Phone className="w-3.5 h-3.5" /> {provider.phone}</span>}
              {provider.email && <span className="flex items-center gap-1"><Mail className="w-3.5 h-3.5" /> {provider.email}</span>}
            </div>
          </div>
          <Link to={`/customer/providers/${providerId}`} className="sc-btn-primary text-sm flex-shrink-0">Book Now</Link>
        </div>
      </div>

      {/* Photos */}
      {photos && photos.length > 0 && (
        <div className="sc-card p-4">
          <h2 className="text-base font-semibold text-[#0F172A] mb-3">Photos</h2>
          <div className="flex gap-3 overflow-x-auto no-scrollbar">
            {photos.map((photo) => (
              <img key={photo.id} src={photo.imageUrl} alt="Portfolio" className="w-40 h-28 rounded-[8px] object-cover flex-shrink-0" />
            ))}
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Services */}
        <div className="lg:col-span-2 space-y-4">
          <div className="sc-card p-4">
            <h2 className="text-base font-semibold text-[#0F172A] mb-3">Services</h2>
            {services.length === 0 ? <p className="text-sm text-[#64748B]">No services listed.</p> : (
              <div className="divide-y divide-[#E2E8F0]">
                {services.filter((s) => s.active).map((s) => (
                  <div key={s.id} className="py-3 flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-[#0F172A]">{s.name}</p>
                      <span className="tag-pill mt-0.5">{s.category}</span>
                      {s.durationMinutes && <span className="text-xs text-[#94A3B8] ml-2">{s.durationMinutes} min</span>}
                    </div>
                    <div className="flex items-center gap-3">
                      <p className="text-sm font-bold text-[#0F172A]">{formatPrice(s.price)}</p>
                      <Link to={`/customer/providers/${providerId}`} className="sc-btn-outline text-xs px-3 py-1">
                        Book
                      </Link>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Reviews */}
          <div className="sc-card p-4">
            <h2 className="text-base font-semibold text-[#0F172A] mb-3">Reviews</h2>
            {reviewList.length === 0 ? <p className="text-sm text-[#64748B]">No reviews yet.</p> : (
              <>
                <div className="space-y-4">
                  {reviewList.map((r) => (
                    <div key={r.id} className="border-b border-[#E2E8F0] pb-3 last:border-0">
                      <div className="flex items-center gap-2 mb-1">
                        <StarRating value={r.rating} readonly size="sm" />
                        <span className="text-xs text-[#94A3B8]">{formatDate(r.createdAt)}</span>
                      </div>
                      {r.comment && <p className="text-sm text-[#0F172A]">{r.comment}</p>}
                    </div>
                  ))}
                </div>
                <Pagination page={reviewPage} totalPages={reviews?.totalPages ?? 1} onPageChange={setReviewPage} />
              </>
            )}
          </div>
        </div>

        {/* Availability sidebar */}
        <div>
          <div className="sc-card p-4">
            <h2 className="text-base font-semibold text-[#0F172A] mb-3 flex items-center gap-2">
              <Clock className="w-4 h-4 text-[#16A34A]" /> Availability
            </h2>
            {!availability || availability.length === 0 ? <p className="text-sm text-[#64748B]">No availability set.</p> : (
              <div className="space-y-2">
                {availability.filter((a) => a.active).map((a) => (
                  <div key={a.id} className="flex items-center justify-between text-sm">
                    <span className="font-medium text-[#0F172A]">{a.dayOfWeek.charAt(0) + a.dayOfWeek.slice(1).toLowerCase()}</span>
                    <span className="text-[#64748B]">{formatTime(a.startTime)} – {formatTime(a.endTime)}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
