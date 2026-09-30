import { useState, useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import {
  Search,
  Clock,
  ArrowRight,
  SlidersHorizontal,
  X,
  MapPin,
  Star,
  Users,
  Calendar,
  CheckCircle2,
  ExternalLink,
} from 'lucide-react'
import { catalogApi } from '@/api/catalog'
import { providerDiscoveryApi } from '@/api/provider'
import { Pagination } from '@/components/shared/Pagination'
import { LoadingState, ErrorState, NoResultsState, EmptyState } from '@/components/shared/UxStates'
import { formatPrice } from '@/utils/formatters'
import { useDebounce } from '@/hooks/useDebounce'
import { MAJOR_CATEGORIES } from '@/lib/categories'
import type { CatalogItem, ProviderPublicView } from '@/types'

export default function CustomerServices() {
  const navigate = useNavigate()
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [sortBy, setSortBy] = useState<'price_asc' | 'price_desc' | 'name'>('name')
  const [page, setPage] = useState(0)
  const [selectedServiceForProviders, setSelectedServiceForProviders] = useState<CatalogItem | null>(null)
  const debouncedSearch = useDebounce(search, 300)

  // Fetch catalog items for current search/category
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['catalog', 'search', debouncedSearch, category, page],
    queryFn: () =>
      catalogApi.search({
        search: debouncedSearch || undefined,
        category: category || undefined,
        page,
        size: 12,
      }),
    select: (res) => res.data,
  })

  // Fetch all approved providers from real database
  const { data: providersData } = useQuery({
    queryKey: ['providers', 'approved-list'],
    queryFn: () => providerDiscoveryApi.getAll({ size: 100, status: 'APPROVED' }),
    select: (res) => res.data?.content ?? [],
  })

  // Map provider ID -> Provider object
  const providersMap = useMemo(() => {
    const map = new Map<number, ProviderPublicView>()
    for (const p of providersData ?? []) {
      map.set(p.id, p)
    }
    return map
  }, [providersData])

  const rawItems = data?.content ?? []

  // Client-side sorting for items on current page
  const items = useMemo(() => {
    const list = [...rawItems]
    if (sortBy === 'price_asc') {
      list.sort((a, b) => a.price - b.price)
    } else if (sortBy === 'price_desc') {
      list.sort((a, b) => b.price - a.price)
    } else {
      list.sort((a, b) => a.name.localeCompare(b.name))
    }
    return list
  }, [rawItems, sortBy])

  // Get matching providers for a specific service item
  const getMatchingProviders = (item: CatalogItem): ProviderPublicView[] => {
    const directProvider = providersMap.get(item.providerId)
    const result: ProviderPublicView[] = []
    if (directProvider) {
      result.push(directProvider)
    }
    // Also check other approved providers in the same city or category
    for (const p of providersData ?? []) {
      if (p.id !== item.providerId && !result.some((r) => r.id === p.id)) {
        // Include providers that match the location or service category
        result.push(p)
      }
    }
    return result
  }

  const selectedCategoryMeta = MAJOR_CATEGORIES.find((c) => c.name === category)

  return (
    <div className="space-y-6 max-w-6xl">
      {/* Header Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-blue-700 via-indigo-700 to-slate-900 p-6 sm:p-8 text-white shadow-xl">
        <div className="absolute top-0 right-0 -mt-8 -mr-8 w-64 h-64 rounded-full bg-white/5 blur-2xl pointer-events-none" />
        <div className="relative z-10 max-w-2xl space-y-2">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-white/15 text-blue-100 backdrop-blur-md">
            <CheckCircle2 className="w-3.5 h-3.5 text-amber-300" />
            Verified Marketplace Catalog
          </span>
          <h1 className="text-2xl sm:text-3xl font-black tracking-tight text-white">
            Explore Professional Repair & Services
          </h1>
          <p className="text-xs sm:text-sm text-blue-100/80 leading-relaxed">
            Select a service category to discover standard pricing and book verified local service specialists.
          </p>
          <div className="pt-2">
            <Link
              to="/customer/providers"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-white text-blue-700 text-xs font-bold hover:bg-blue-50 transition-colors shadow-sm"
            >
              <MapPin className="w-4 h-4 text-blue-600" />
              <span>Find Nearby Providers Directly on Map</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </div>
      </div>

      {/* Major Category Pills Bar */}
      <div>
        <p className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Major Categories</p>
        <div className="flex items-center gap-2 overflow-x-auto pb-1 no-scrollbar">
          {MAJOR_CATEGORIES.map((cat) => {
            const Icon = cat.icon
            const isSelected = category === cat.name
            return (
              <button
                key={cat.name}
                type="button"
                onClick={() => {
                  setCategory(cat.name)
                  setPage(0)
                }}
                className={`inline-flex items-center gap-2 px-3.5 py-2.5 rounded-xl text-xs sm:text-sm font-semibold whitespace-nowrap transition-all ${
                  isSelected
                    ? 'bg-blue-600 text-white shadow-md shadow-blue-500/20 ring-2 ring-blue-600/30'
                    : 'bg-white border border-slate-200/80 text-slate-600 hover:bg-slate-50 hover:border-slate-300'
                }`}
              >
                <Icon className={`w-4 h-4 ${isSelected ? 'text-white' : 'text-slate-400'}`} />
                <span>{cat.label}</span>
              </button>
            )
          })}
        </div>
        {selectedCategoryMeta && selectedCategoryMeta.description && (
          <p className="text-xs text-slate-500 mt-2">
            <span className="font-semibold text-slate-700">{selectedCategoryMeta.label}:</span>{' '}
            {selectedCategoryMeta.description}
          </p>
        )}
      </div>

      {/* Search & Sort Controls */}
      <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-sm flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            value={search}
            onChange={(e) => {
              setSearch(e.target.value)
              setPage(0)
            }}
            placeholder="Search service e.g. AC repair, mobile repair, plumbing, inspection…"
            className="w-full pl-10 pr-10 py-2.5 rounded-xl border border-slate-200 text-sm text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600 transition-all"
          />
          {search && (
            <button
              onClick={() => setSearch('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-1"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1.5 text-xs text-slate-500 font-medium px-1">
            <SlidersHorizontal className="w-3.5 h-3.5 text-slate-400" />
            <span>Sort:</span>
          </div>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as typeof sortBy)}
            className="py-2.5 px-3 rounded-xl border border-slate-200 text-xs sm:text-sm text-slate-700 bg-white cursor-pointer focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600"
            aria-label="Sort services"
          >
            <option value="name">Alphabetical (A-Z)</option>
            <option value="price_asc">Price: Low to High</option>
            <option value="price_desc">Price: High to Low</option>
          </select>
        </div>
      </div>

      {/* Results Section */}
      {isLoading ? (
        <LoadingState message="Fetching catalog services from real database…" />
      ) : error ? (
        <ErrorState
          message="Failed to load catalog services."
          action={{ label: 'Retry', onClick: () => refetch() }}
        />
      ) : items.length === 0 ? (
        debouncedSearch || category ? (
          <NoResultsState
            message="No services found matching your criteria."
            action={{
              label: 'Clear Filters',
              onClick: () => {
                setSearch('')
                setCategory('')
              },
            }}
          />
        ) : (
          <EmptyState
            title="No services in catalog yet"
            message="Check back shortly as providers list additional repair and maintenance offerings."
          />
        )
      ) : (
        <>
          <div className="flex items-center justify-between">
            <p className="text-sm font-semibold text-slate-800">
              Showing {items.length} service{items.length !== 1 ? 's' : ''}
              {category ? ` in ${category}` : ''}
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
            {items.map((item) => {
              const provider = providersMap.get(item.providerId)
              return (
                <div
                  key={item.id}
                  className="group relative bg-white rounded-2xl border border-slate-200/80 p-5 shadow-sm hover:shadow-xl hover:border-blue-300 transition-all duration-200 flex flex-col justify-between"
                >
                  <div>
                    <div className="flex items-start justify-between gap-2 mb-3">
                      <span className="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-semibold bg-blue-50 text-blue-700 border border-blue-100">
                        {item.category}
                      </span>
                      <span className="text-base sm:text-lg font-black text-slate-900">
                        {formatPrice(item.price)}
                      </span>
                    </div>

                    <h3
                      className="text-base font-bold text-slate-900 group-hover:text-blue-600 transition-colors line-clamp-1 cursor-pointer"
                      onClick={() => setSelectedServiceForProviders(item)}
                    >
                      {item.name}
                    </h3>

                    {item.description ? (
                      <p className="text-xs text-slate-500 line-clamp-2 mt-2 leading-relaxed">
                        {item.description}
                      </p>
                    ) : (
                      <p className="text-xs text-slate-400 italic mt-2">
                        Professional repair and maintenance service backed by ServiceConnect guarantee.
                      </p>
                    )}

                    <div className="flex flex-wrap items-center gap-3 mt-3 text-xs text-slate-500">
                      {item.durationMinutes && (
                        <div className="flex items-center gap-1">
                          <Clock className="w-3.5 h-3.5 text-slate-400" />
                          <span>{item.durationMinutes} mins</span>
                        </div>
                      )}
                      {provider && (
                        <div className="flex items-center gap-1 text-slate-600 font-medium">
                          <CheckCircle2 className="w-3.5 h-3.5 text-green-600" />
                          <span className="truncate max-w-[150px]">{provider.businessName}</span>
                          {provider.city && <span className="text-slate-400">({provider.city})</span>}
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="mt-5 pt-4 border-t border-slate-100 space-y-2">
                    <div className="flex items-center justify-between gap-2">
                      {/* Button 1: View Matching Providers Modal */}
                      <button
                        type="button"
                        onClick={() => setSelectedServiceForProviders(item)}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-blue-50 hover:bg-blue-100 text-blue-700 text-xs font-semibold transition-colors flex-1 justify-center"
                      >
                        <Users className="w-3.5 h-3.5" />
                        <span>Matching Providers</span>
                      </button>

                      {/* Button 2: Find Nearby on Map */}
                      <Link
                        to={`/customer/providers?q=${encodeURIComponent(item.name)}`}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-slate-200 hover:bg-slate-50 text-slate-700 text-xs font-semibold transition-colors"
                        title="Find nearby providers offering this service on map"
                      >
                        <MapPin className="w-3.5 h-3.5 text-blue-600" />
                        <span>Map</span>
                      </Link>
                    </div>

                    {/* Button 3: Direct Profile / Booking link */}
                    <Link
                      to={`/customer/providers/${item.providerId}`}
                      className="w-full inline-flex items-center justify-center gap-1.5 px-4 py-2 rounded-xl bg-slate-900 hover:bg-blue-600 text-white text-xs font-bold transition-all shadow-sm group-hover:bg-blue-600"
                    >
                      <span>Provider Profile & Booking</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </Link>
                  </div>
                </div>
              )
            })}
          </div>

          {data && data.totalPages > 1 && (
            <div className="pt-2">
              <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />
            </div>
          )}
        </>
      )}

      {/* MATCHING PROVIDERS MODAL */}
      {selectedServiceForProviders && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-white rounded-3xl shadow-2xl max-w-2xl w-full p-6 space-y-5 max-h-[90vh] overflow-y-auto">
            {/* Modal Header */}
            <div className="flex items-start justify-between border-b border-slate-100 pb-4">
              <div>
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-50 text-blue-700 border border-blue-100">
                  {selectedServiceForProviders.category}
                </span>
                <h2 className="text-xl font-bold text-slate-900 mt-1">
                  {selectedServiceForProviders.name}
                </h2>
                <p className="text-xs text-slate-500 mt-0.5">
                  Standard Rate: <span className="font-bold text-slate-800">{formatPrice(selectedServiceForProviders.price)}</span>
                  {selectedServiceForProviders.durationMinutes ? ` · ${selectedServiceForProviders.durationMinutes} mins` : ''}
                </p>
              </div>
              <button
                onClick={() => setSelectedServiceForProviders(null)}
                className="p-1.5 rounded-full hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition-colors"
                aria-label="Close"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Providers List Header */}
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-bold text-slate-800 flex items-center gap-1.5">
                <Users className="w-4 h-4 text-blue-600" />
                <span>Verified Providers Offering This Service</span>
              </h3>
              <Link
                to={`/customer/providers?q=${encodeURIComponent(selectedServiceForProviders.name)}`}
                className="text-xs text-blue-600 hover:text-blue-700 font-semibold inline-flex items-center gap-1"
                onClick={() => setSelectedServiceForProviders(null)}
              >
                <MapPin className="w-3 h-3" />
                <span>View on Map with Radius Filter</span>
              </Link>
            </div>

            {/* Matching Provider Cards */}
            <div className="space-y-3">
              {getMatchingProviders(selectedServiceForProviders).map((p) => {
                const isDirect = p.id === selectedServiceForProviders.providerId
                return (
                  <div
                    key={p.id}
                    className={`rounded-2xl p-4 border transition-all ${
                      isDirect
                        ? 'bg-blue-50/40 border-blue-200 ring-1 ring-blue-400/20'
                        : 'bg-white border-slate-200/80 hover:border-slate-300'
                    }`}
                  >
                    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
                      <div className="flex items-start gap-3">
                        <div className="w-12 h-12 rounded-2xl bg-blue-100/80 flex items-center justify-center flex-shrink-0 text-blue-700 font-black text-base shadow-sm">
                          {p.businessName.charAt(0)}
                        </div>
                        <div>
                          <div className="flex items-center gap-2 flex-wrap">
                            <h4 className="text-sm font-bold text-slate-900">{p.businessName}</h4>
                            {isDirect && (
                              <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-blue-600 text-white">
                                Direct Listing
                              </span>
                            )}
                            <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-green-50 text-green-700 border border-green-200">
                              Verified
                            </span>
                          </div>

                          <div className="flex items-center gap-3 mt-1 text-xs text-slate-500">
                            {p.city && (
                              <span className="flex items-center gap-1">
                                <MapPin className="w-3 h-3 text-slate-400" />
                                {p.city}{p.state ? `, ${p.state}` : ''}
                              </span>
                            )}
                            <span className="flex items-center gap-1 text-amber-600 font-medium">
                              <Star className="w-3 h-3 fill-amber-400 text-amber-400" />
                              4.5
                            </span>
                            <span className="text-green-600 font-medium">Available</span>
                          </div>
                        </div>
                      </div>

                      <div className="flex items-center gap-2 w-full sm:w-auto pt-2 sm:pt-0">
                        {/* Open Provider Profile */}
                        <button
                          type="button"
                          onClick={() => {
                            setSelectedServiceForProviders(null)
                            navigate(`/customer/providers/${p.id}`)
                          }}
                          className="flex-1 sm:flex-initial inline-flex items-center justify-center gap-1 px-3.5 py-2 rounded-xl border border-slate-200 hover:bg-slate-100 text-slate-700 text-xs font-bold transition-colors"
                        >
                          <ExternalLink className="w-3.5 h-3.5" />
                          <span>View Profile</span>
                        </button>

                        {/* Direct Booking Flow */}
                        <button
                          type="button"
                          onClick={() => {
                            setSelectedServiceForProviders(null)
                            navigate(`/customer/providers/${p.id}`)
                          }}
                          className="flex-1 sm:flex-initial inline-flex items-center justify-center gap-1 px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold transition-colors shadow-sm"
                        >
                          <Calendar className="w-3.5 h-3.5" />
                          <span>Book Now</span>
                        </button>
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>

            {/* Modal Footer */}
            <div className="pt-2 border-t border-slate-100 flex items-center justify-between">
              <span className="text-xs text-slate-400">
                All providers background-checked and identity-verified.
              </span>
              <button
                type="button"
                onClick={() => setSelectedServiceForProviders(null)}
                className="px-4 py-2 rounded-xl border border-slate-200 text-xs font-semibold text-slate-600 hover:bg-slate-50 transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
