import { useState, useEffect, useMemo, useCallback } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet'
import L from 'leaflet'
import {
  Search,
  MapPin,
  List,
  Map as MapIcon,
  Navigation,
  Star,
  Clock,
  X,
  Filter,
  CheckCircle2,
  Calendar,
  Sparkles,
} from 'lucide-react'
import { providerDiscoveryApi } from '@/api/provider'
import { catalogApi } from '@/api/catalog'
import { filterByRadius, formatDistance } from '@/lib/haversine'
import { useDebounce } from '@/hooks/useDebounce'
import { LoadingState, ErrorState, EmptyState } from '@/components/shared/UxStates'
import { MAJOR_CATEGORIES, POPULAR_SERVICE_SEARCHES } from '@/lib/categories'
import type { MapViewMode, Coordinates, CatalogItem, ProviderPublicView } from '@/types'

// ============================================================
// ProviderDiscovery — Primary Service Discovery Page
//
// Allows customers to answer: "What service do you need?"
// (e.g. AC repair, Mobile repair, Refrigerator, Plumbing)
// and directly finds nearby verified providers offering that service.
//
// Map markers represent PROVIDERS and stay synchronized with list.
// ============================================================

// Custom Leaflet DivIcon for providers
const createProviderIcon = (name: string, isSelected: boolean) =>
  new L.DivIcon({
    className: 'sc-marker-container',
    html: `
      <div style="
        background: ${isSelected ? '#2563EB' : '#0F172A'};
        color: white;
        padding: 4px 8px;
        border-radius: 9999px;
        font-size: 11px;
        font-weight: 700;
        white-space: nowrap;
        box-shadow: 0 4px 12px rgba(0,0,0,0.25);
        border: 2px solid white;
        display: flex;
        align-items: center;
        gap: 4px;
        cursor: pointer;
        transform: translate(-50%, -50%);
      ">
        <span style="display:inline-block;width:6px;height:6px;border-radius:50%;background:${isSelected ? '#FBBF24' : '#10B981'}"></span>
        ${name.length > 18 ? name.substring(0, 16) + '…' : name}
      </div>
    `,
    iconSize: [0, 0],
    iconAnchor: [0, 0],
  })

const userIcon = new L.DivIcon({
  className: '',
  html: '<div style="width:16px;height:16px;background:#2563EB;border:3px solid white;border-radius:50%;box-shadow:0 2px 8px rgba(37,99,235,0.6)"></div>',
  iconSize: [16, 16],
  iconAnchor: [8, 8],
})

// Quick reference locations for testing & instant radius filtering
const PRESET_LOCATIONS: { name: string; label: string; coords: Coordinates }[] = [
  { name: 'bangalore', label: 'Bangalore (Apex)', coords: { lat: 12.9716, lng: 77.5946 } },
  { name: 'chennai', label: 'Chennai (SK Services)', coords: { lat: 13.0827, lng: 80.2707 } },
]

function RecenterButton({ center }: { center: Coordinates }) {
  const map = useMap()
  return (
    <button
      onClick={() => map.flyTo([center.lat, center.lng], 13)}
      className="sc-btn-primary text-xs px-3 py-1.5 absolute top-3 right-3 z-[1000] gap-1 shadow-md"
      aria-label="Recenter map"
    >
      <Navigation className="w-3.5 h-3.5" /> Recenter
    </button>
  )
}

function MapFocus({ selectedLocation }: { selectedLocation?: { lat: number; lng: number } | null }) {
  const map = useMap()
  useEffect(() => {
    if (selectedLocation?.lat && selectedLocation?.lng) {
      map.flyTo([selectedLocation.lat, selectedLocation.lng], 14, { duration: 0.8 })
    }
  }, [selectedLocation, map])
  return null
}

interface EnrichedProvider extends ProviderPublicView {
  distanceKm?: number
  matchedServices: string[]
  matchedCategories: string[]
  lowestPrice?: number
}

export default function ProviderDiscovery() {
  const [searchParams] = useSearchParams()
  const initialSearch = searchParams.get('q') || ''

  const [search, setSearch] = useState(initialSearch)
  const [category, setCategory] = useState('')
  const [radius, setRadius] = useState<number>(50) // Default 50km
  const [viewMode, setViewMode] = useState<MapViewMode>('split')
  const [userLocation, setUserLocation] = useState<Coordinates | null>({ lat: 12.9716, lng: 77.5946 }) // Default Bangalore
  const [locationName, setLocationName] = useState('Bangalore')
  const [locationError, setLocationError] = useState('')
  const [selectedProviderId, setSelectedProviderId] = useState<number | null>(null)
  const [showSuggestions, setShowSuggestions] = useState(false)
  const debouncedSearch = useDebounce(search, 300)

  // Try GPS geolocation on mount
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          setUserLocation({ lat: pos.coords.latitude, lng: pos.coords.longitude })
          setLocationName('My GPS Location')
          setLocationError('')
        },
        () => {
          setLocationError('GPS access denied; using Bangalore reference.')
        },
        { enableHighAccuracy: false, timeout: 5000 },
      )
    }
  }, [])

  // If query param 'q' changes in URL, update search state
  useEffect(() => {
    const q = searchParams.get('q')
    if (q && q !== search) {
      setSearch(q)
    }
  }, [searchParams])

  // Fetch approved providers from real PostgreSQL database
  const {
    data: providerData,
    isLoading: providersLoading,
    error: providersError,
    refetch: refetchProviders,
  } = useQuery({
    queryKey: ['providers', 'discovery'],
    queryFn: () => providerDiscoveryApi.getAll({ size: 100, status: 'APPROVED' }),
    select: (res) => res.data,
  })

  // Fetch all active catalog items from real PostgreSQL database
  const { data: catalogData, isLoading: catalogLoading } = useQuery({
    queryKey: ['catalog', 'all-for-discovery'],
    queryFn: () => catalogApi.search({ size: 200 }),
    select: (res) => res.data,
  })

  // Map of providerId -> CatalogItem[]
  const providerCatalogMap = useMemo(() => {
    const map = new Map<number, CatalogItem[]>()
    const items = catalogData?.content ?? []
    for (const item of items) {
      if (!item.active) continue
      const list = map.get(item.providerId) ?? []
      list.push(item)
      map.set(item.providerId, list)
    }
    return map
  }, [catalogData])

  const hasFilter = !!(debouncedSearch.trim() || category)

  // Find which providers offer services matching the search term or category
  const matchingProviderIds = useMemo(() => {
    if (!hasFilter) return null // null means show all
    const items = catalogData?.content ?? []
    const q = debouncedSearch.toLowerCase().trim()

    const matched = items.filter((item) => {
      if (!item.active) return false
      // Category filter
      if (category && item.category !== category) return false
      // Search term matching
      if (q) {
        const nameMatch = item.name.toLowerCase().includes(q)
        const descMatch = item.description?.toLowerCase().includes(q) ?? false
        const catMatch = item.category.toLowerCase().includes(q)
        if (!nameMatch && !descMatch && !catMatch) return false
      }
      return true
    })

    return new Set(matched.map((item) => item.providerId))
  }, [catalogData, debouncedSearch, category, hasFilter])

  // Get matched service names and prices for a specific provider
  const getMatchedServicesForProvider = useCallback(
    (providerId: number) => {
      const items = providerCatalogMap.get(providerId) ?? []
      if (!items.length) return { services: [], categories: [], lowestPrice: undefined }

      const q = debouncedSearch.toLowerCase().trim()
      let filtered = items

      if (hasFilter) {
        filtered = items.filter((item) => {
          if (category && item.category !== category) return false
          if (q) {
            const nameMatch = item.name.toLowerCase().includes(q)
            const descMatch = item.description?.toLowerCase().includes(q) ?? false
            const catMatch = item.category.toLowerCase().includes(q)
            if (!nameMatch && !descMatch && !catMatch) return false
          }
          return true
        })
      }

      const services = [...new Set(filtered.map((i) => i.name))]
      const categories = [...new Set(filtered.map((i) => i.category))]
      const prices = filtered.map((i) => i.price).filter(Boolean)
      const lowestPrice = prices.length > 0 ? Math.min(...prices) : undefined

      return { services, categories, lowestPrice }
    },
    [providerCatalogMap, debouncedSearch, category, hasFilter],
  )

  // Filtered and enriched providers (service match + radius calculation)
  const filteredProviders: EnrichedProvider[] = useMemo(() => {
    let providers = providerData?.content ?? []

    // 1. Service/catalog filter
    if (matchingProviderIds !== null) {
      providers = providers.filter((p) => matchingProviderIds.has(p.id))
    }

    // 2. Also match provider business name / city directly
    if (debouncedSearch.trim() && matchingProviderIds !== null) {
      const q = debouncedSearch.toLowerCase().trim()
      const allProviders = providerData?.content ?? []
      const nameMatched = allProviders.filter(
        (p) =>
          (p.businessName.toLowerCase().includes(q) || p.city?.toLowerCase().includes(q)) &&
          !matchingProviderIds.has(p.id),
      )
      providers = [...providers, ...nameMatched]
    }

    // 3. Location and radius filtering
    if (userLocation && radius > 0) {
      const withDist = filterByRadius(providers, userLocation.lat, userLocation.lng, radius)
      return withDist.map((p) => {
        const info = getMatchedServicesForProvider(p.id)
        return {
          ...p,
          matchedServices: info.services,
          matchedCategories: info.categories,
          lowestPrice: info.lowestPrice,
        }
      })
    }

    // If radius is 0 ("All distances") or userLocation is null:
    return providers.map((p) => {
      const info = getMatchedServicesForProvider(p.id)
      let dist: number | undefined = undefined
      if (userLocation && p.latitude != null && p.longitude != null) {
        // Compute distance even without filtering
        const rad = filterByRadius([p], userLocation.lat, userLocation.lng, 99999)
        dist = rad[0]?.distanceKm
      }
      return {
        ...p,
        distanceKm: dist,
        matchedServices: info.services,
        matchedCategories: info.categories,
        lowestPrice: info.lowestPrice,
      }
    })
  }, [
    providerData,
    debouncedSearch,
    matchingProviderIds,
    userLocation,
    radius,
    getMatchedServicesForProvider,
  ])

  const mapCenter: [number, number] = userLocation
    ? [userLocation.lat, userLocation.lng]
    : [12.9716, 77.5946]

  const isLoading = providersLoading || catalogLoading

  const formatPriceCompact = (price: number) => {
    if (price >= 1000) return `₹${(price / 1000).toFixed(1)}k`
    return `₹${price}`
  }

  const resultTitle = useMemo(() => {
    if (!hasFilter) {
      return `${filteredProviders.length} Verified Provider${filteredProviders.length !== 1 ? 's' : ''}`
    }
    const parts: string[] = []
    if (debouncedSearch.trim()) parts.push(`"${debouncedSearch.trim()}"`)
    if (category) parts.push(category)
    return `${filteredProviders.length} provider${filteredProviders.length !== 1 ? 's' : ''} offering ${parts.join(' in ')}`
  }, [filteredProviders.length, debouncedSearch, category, hasFilter])

  return (
    <div className="page-container py-4 space-y-4">
      {/* Breadcrumb */}
      <nav className="text-xs text-[#64748B]" aria-label="Breadcrumb">
        <Link to="/" className="hover:text-[#2563EB]">
          ServiceConnect
        </Link>
        <span className="mx-1">›</span>
        <span className="text-[#0F172A] font-medium">Find Nearby Providers</span>
      </nav>

      {/* Header Headline */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">
            What service do you need?
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
            Search for any repair or maintenance work to instantly locate nearby verified providers.
          </p>
        </div>
        <Link
          to="/customer/services"
          className="inline-flex items-center gap-1.5 text-xs text-blue-600 hover:text-blue-700 font-semibold self-start sm:self-auto"
        >
          <span>Browse All Categories Catalog</span>
          <span>→</span>
        </Link>
      </div>

      {/* PRIMARY SERVICE SEARCH BAR */}
      <div className="relative">
        <div className="relative">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
          <input
            value={search}
            onChange={(e) => {
              setSearch(e.target.value)
              setShowSuggestions(true)
            }}
            onFocus={() => setShowSuggestions(true)}
            placeholder="Search service e.g. AC repair, TV repair, mobile repair, laptop repair, plumbing…"
            className="w-full pl-12 pr-10 py-3.5 rounded-2xl border-2 border-slate-200 text-sm sm:text-base text-slate-900 placeholder-slate-400 focus:outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-500/10 transition-all bg-white shadow-sm font-medium"
            aria-label="What service do you need?"
          />
          {search && (
            <button
              onClick={() => {
                setSearch('')
                setShowSuggestions(false)
              }}
              className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-1"
              aria-label="Clear search"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* Popular Service Suggestions Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar pt-2">
          <span className="text-[11px] font-bold text-slate-400 whitespace-nowrap uppercase tracking-wider flex items-center gap-1">
            <Sparkles className="w-3 h-3 text-amber-500" /> Popular:
          </span>
          {POPULAR_SERVICE_SEARCHES.map((term) => (
            <button
              key={term}
              type="button"
              onClick={() => {
                setSearch(term)
                setShowSuggestions(false)
              }}
              className={`px-3 py-1 rounded-full text-xs font-semibold whitespace-nowrap transition-all ${
                search.toLowerCase() === term.toLowerCase()
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'bg-slate-100 hover:bg-slate-200 text-slate-700'
              }`}
            >
              {term}
            </button>
          ))}
        </div>
      </div>

      {/* Major Category Filter Bar */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-1 no-scrollbar">
        {MAJOR_CATEGORIES.map((cat) => {
          const Icon = cat.icon
          const isSelected = category === cat.name
          return (
            <button
              key={cat.name}
              type="button"
              onClick={() => setCategory(isSelected ? '' : cat.name)}
              className={`inline-flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold whitespace-nowrap transition-all ${
                isSelected
                  ? 'bg-blue-600 text-white shadow-md shadow-blue-500/20'
                  : 'bg-white border border-slate-200/80 text-slate-600 hover:bg-slate-50'
              }`}
            >
              <Icon className={`w-3.5 h-3.5 ${isSelected ? 'text-white' : 'text-slate-400'}`} />
              <span>{cat.label}</span>
            </button>
          )
        })}
      </div>

      {/* Location / Radius & View Controls */}
      <div className="flex flex-wrap items-center justify-between gap-3 bg-white p-3 rounded-2xl border border-slate-200/80 shadow-sm text-xs">
        {/* Location selector */}
        <div className="flex items-center gap-2 flex-wrap">
          <div className="flex items-center gap-1 text-slate-500 font-medium">
            <MapPin className="w-4 h-4 text-blue-600" />
            <span>Location:</span>
          </div>
          <div className="flex items-center gap-1 bg-slate-100 p-1 rounded-xl">
            {PRESET_LOCATIONS.map((loc) => {
              const isSelected = locationName === loc.name || locationName.includes(loc.label.split(' ')[0])
              return (
                <button
                  key={loc.name}
                  type="button"
                  onClick={() => {
                    setUserLocation(loc.coords)
                    setLocationName(loc.name)
                  }}
                  className={`px-2.5 py-1 rounded-lg font-semibold transition-all ${
                    isSelected
                      ? 'bg-white text-blue-700 shadow-sm'
                      : 'text-slate-600 hover:text-slate-900'
                  }`}
                >
                  {loc.label}
                </button>
              )
            })}
          </div>

          {/* Radius selector */}
          <div className="flex items-center gap-1 ml-2">
            <span className="text-slate-500 font-medium">Radius:</span>
            <select
              value={radius}
              onChange={(e) => setRadius(Number(e.target.value))}
              className="py-1 px-2.5 rounded-lg border border-slate-200 bg-white font-semibold text-slate-700 focus:outline-none focus:border-blue-600"
              aria-label="Search Radius"
            >
              <option value={5}>Within 5 km</option>
              <option value={10}>Within 10 km</option>
              <option value={25}>Within 25 km</option>
              <option value={50}>Within 50 km</option>
              <option value={100}>Within 100 km</option>
              <option value={500}>Within 500 km</option>
              <option value={0}>All Distances</option>
            </select>
          </div>
        </div>

        {/* View Mode (Split, Map Only, List Only) */}
        <div className="flex border border-slate-200 rounded-xl overflow-hidden shadow-sm">
          <button
            type="button"
            onClick={() => setViewMode('split')}
            className={`px-3 py-1.5 font-semibold ${
              viewMode === 'split' ? 'bg-blue-600 text-white' : 'bg-white text-slate-600 hover:bg-slate-50'
            }`}
          >
            Split
          </button>
          <button
            type="button"
            onClick={() => setViewMode('map-only')}
            className={`px-3 py-1.5 border-x border-slate-200 font-semibold ${
              viewMode === 'map-only' ? 'bg-blue-600 text-white' : 'bg-white text-slate-600 hover:bg-slate-50'
            }`}
            aria-label="Map view only"
          >
            <MapIcon className="w-3.5 h-3.5 inline" />
          </button>
          <button
            type="button"
            onClick={() => setViewMode('list-only')}
            className={`px-3 py-1.5 font-semibold ${
              viewMode === 'list-only' ? 'bg-blue-600 text-white' : 'bg-white text-slate-600 hover:bg-slate-50'
            }`}
            aria-label="List view only"
          >
            <List className="w-3.5 h-3.5 inline" />
          </button>
        </div>
      </div>

      {/* Active Filter Badges */}
      {hasFilter && (
        <div className="flex items-center gap-2 text-xs flex-wrap">
          <Filter className="w-3.5 h-3.5 text-blue-600" />
          <span className="text-slate-500 font-medium">Filtering by:</span>
          {debouncedSearch.trim() && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-blue-50 text-blue-700 font-semibold border border-blue-200">
              "{debouncedSearch.trim()}"
              <button
                onClick={() => setSearch('')}
                className="hover:text-blue-900"
                aria-label="Remove search filter"
              >
                <X className="w-3 h-3" />
              </button>
            </span>
          )}
          {category && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-green-50 text-green-700 font-semibold border border-green-200">
              {category}
              <button
                onClick={() => setCategory('')}
                className="hover:text-green-900"
                aria-label="Remove category filter"
              >
                <X className="w-3 h-3" />
              </button>
            </span>
          )}
          <button
            type="button"
            onClick={() => {
              setSearch('')
              setCategory('')
            }}
            className="text-slate-500 hover:text-slate-800 underline font-medium ml-1"
          >
            Reset filters
          </button>
        </div>
      )}

      {/* Results Count Banner */}
      <div className="flex items-center justify-between text-sm font-bold text-slate-800">
        <span>{resultTitle}</span>
        {radius > 0 && userLocation && (
          <span className="text-xs font-normal text-slate-500">
            Within {radius} km of selected location
          </span>
        )}
      </div>

      {/* Main Content Area: Synchronized List + Map */}
      <div
        className="flex gap-4 relative"
        style={{ height: 'calc(100vh - 360px)', minHeight: '440px' }}
      >
        {/* PROVIDER LIST */}
        {viewMode !== 'map-only' && (
          <div
            className={`overflow-y-auto space-y-3.5 no-scrollbar pr-1 ${
              viewMode === 'split' ? 'w-[45%] flex-shrink-0' : 'w-full'
            }`}
          >
            {isLoading ? (
              <LoadingState message="Searching matching providers in database…" />
            ) : providersError ? (
              <ErrorState
                message="Failed to load providers from backend."
                action={{ label: 'Retry', onClick: () => refetchProviders() }}
              />
            ) : filteredProviders.length === 0 ? (
              <EmptyState
                title={hasFilter ? 'No matching providers found' : 'No providers in this area'}
                message={
                  hasFilter
                    ? 'Try broadening your search term, selecting All Distances, or switching location.'
                    : 'Try increasing the radius to locate verified specialists.'
                }
              />
            ) : (
              filteredProviders.map((p) => {
                const isSelected = p.id === selectedProviderId
                return (
                  <div
                    key={p.id}
                    id={`provider-card-${p.id}`}
                    className={`rounded-2xl p-4 transition-all duration-200 cursor-pointer border ${
                      isSelected
                        ? 'bg-blue-50/60 border-blue-500 ring-2 ring-blue-500/20 shadow-md'
                        : 'bg-white border-slate-200/80 hover:border-slate-300 hover:shadow-md'
                    }`}
                    onClick={() => setSelectedProviderId(p.id)}
                    role="button"
                    tabIndex={0}
                    onKeyDown={(e) => e.key === 'Enter' && setSelectedProviderId(p.id)}
                  >
                    <div className="flex items-start gap-3">
                      <div className="w-12 h-12 rounded-2xl bg-blue-100 flex items-center justify-center flex-shrink-0 text-blue-700 font-black text-base shadow-sm">
                        {p.businessName.charAt(0)}
                      </div>

                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between gap-1">
                          <h3 className="text-sm font-bold text-slate-900 truncate">
                            {p.businessName}
                          </h3>
                          {p.lowestPrice != null && (
                            <span className="text-xs font-black text-slate-900 bg-slate-100 px-2 py-0.5 rounded-full flex-shrink-0">
                              from {formatPriceCompact(p.lowestPrice)}
                            </span>
                          )}
                        </div>

                        {p.city && (
                          <p className="text-xs text-slate-500 mt-0.5 flex items-center gap-1">
                            <MapPin className="w-3 h-3 text-slate-400" />
                            {p.city}
                            {p.state ? `, ${p.state}` : ''}
                          </p>
                        )}

                        <div className="flex items-center gap-3 mt-1.5 text-xs text-slate-500">
                          <span className="flex items-center gap-1 text-amber-600 font-semibold">
                            <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                            4.5
                          </span>
                          {p.distanceKm != null && (
                            <span className="text-blue-600 font-semibold">
                              · {formatDistance(p.distanceKm)}
                            </span>
                          )}
                          <span className="flex items-center gap-1 text-green-600 font-semibold">
                            <Clock className="w-3 h-3" /> Available
                          </span>
                        </div>

                        {/* Matched Services Tags */}
                        {p.matchedServices.length > 0 ? (
                          <div className="flex flex-wrap gap-1 mt-2.5">
                            {p.matchedServices.slice(0, 3).map((svc) => (
                              <span
                                key={svc}
                                className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-green-50 text-green-700 border border-green-200"
                              >
                                {svc}
                              </span>
                            ))}
                            {p.matchedServices.length > 3 && (
                              <span className="text-[10px] text-slate-400 font-medium px-1">
                                +{p.matchedServices.length - 3} more
                              </span>
                            )}
                          </div>
                        ) : p.matchedCategories.length > 0 ? (
                          <div className="flex flex-wrap gap-1 mt-2.5">
                            {p.matchedCategories.slice(0, 2).map((cat) => (
                              <span
                                key={cat}
                                className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold bg-blue-50 text-blue-700 border border-blue-200"
                              >
                                {cat}
                              </span>
                            ))}
                          </div>
                        ) : null}
                      </div>
                    </div>

                    {/* Action Buttons: Quick Book & Profile */}
                    <div className="flex items-center gap-2 mt-3.5 pt-3 border-t border-slate-100">
                      <Link
                        to={`/customer/providers/${p.id}`}
                        className="flex-1 inline-flex items-center justify-center gap-1 px-3 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold transition-all shadow-sm text-center"
                        onClick={(e) => e.stopPropagation()}
                      >
                        <Calendar className="w-3.5 h-3.5" />
                        <span>Quick Book</span>
                      </Link>
                      <Link
                        to={`/providers/${p.id}`}
                        className="flex-1 inline-flex items-center justify-center gap-1 px-3 py-2 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-700 text-xs font-bold transition-colors text-center"
                        onClick={(e) => e.stopPropagation()}
                      >
                        <span>Profile</span>
                      </Link>
                    </div>
                  </div>
                )
              })
            )}
          </div>
        )}

        {/* SYNCHRONIZED LEAFLET MAP */}
        {viewMode !== 'list-only' && (
          <div
            className={`relative rounded-3xl overflow-hidden border border-slate-200 shadow-sm ${
              viewMode === 'split' ? 'flex-1' : 'w-full'
            }`}
          >
            <MapContainer
              center={mapCenter}
              zoom={11}
              style={{ height: '100%', width: '100%' }}
              scrollWheelZoom
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />

              {/* Smooth map pan when provider is selected */}
              <MapFocus
                selectedLocation={
                  (() => {
                    const sel = filteredProviders.find((p) => p.id === selectedProviderId)
                    return sel?.latitude && sel?.longitude
                      ? { lat: sel.latitude, lng: sel.longitude }
                      : null
                  })()
                }
              />

              {/* Reference User Location Marker */}
              {userLocation && (
                <>
                  <Marker position={[userLocation.lat, userLocation.lng]} icon={userIcon}>
                    <Popup>
                      <div className="text-xs font-semibold">
                        Reference Location ({locationName})
                      </div>
                    </Popup>
                  </Marker>
                  <RecenterButton center={userLocation} />
                </>
              )}

              {/* Real Provider Markers on Map */}
              {filteredProviders.map((p) => {
                if (!p.latitude || !p.longitude) return null
                const isSelected = p.id === selectedProviderId
                return (
                  <Marker
                    key={p.id}
                    position={[p.latitude, p.longitude]}
                    icon={createProviderIcon(p.businessName, isSelected)}
                    eventHandlers={{
                      click: () => setSelectedProviderId(p.id),
                    }}
                  >
                    <Popup>
                      <div className="p-1 min-w-[200px] space-y-2">
                        <div>
                          <div className="flex items-center gap-1.5">
                            <span className="font-bold text-sm text-slate-900">{p.businessName}</span>
                            <span className="text-[10px] px-1.5 py-0.2 rounded bg-green-50 text-green-700 font-semibold border border-green-200">
                              Verified
                            </span>
                          </div>
                          {p.city && (
                            <p className="text-slate-500 text-xs mt-0.5">{p.city}, {p.state}</p>
                          )}
                        </div>

                        {p.matchedServices.length > 0 && (
                          <div className="text-xs text-green-700 bg-green-50 p-1.5 rounded-lg border border-green-200">
                            <span className="font-bold">Offers: </span>
                            {p.matchedServices.slice(0, 2).join(', ')}
                          </div>
                        )}

                        {p.lowestPrice != null && (
                          <p className="text-xs font-semibold text-slate-800">
                            Service from {formatPriceCompact(p.lowestPrice)}
                          </p>
                        )}

                        <div className="flex gap-2 pt-1">
                          <Link
                            to={`/customer/providers/${p.id}`}
                            className="flex-1 bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold py-1.5 px-2 rounded-lg text-center"
                          >
                            Quick Book
                          </Link>
                          <Link
                            to={`/providers/${p.id}`}
                            className="flex-1 border border-slate-200 hover:bg-slate-50 text-slate-700 text-xs font-semibold py-1.5 px-2 rounded-lg text-center"
                          >
                            Profile
                          </Link>
                        </div>
                      </div>
                    </Popup>
                  </Marker>
                )
              })}
            </MapContainer>
          </div>
        )}
      </div>
    </div>
  )
}
