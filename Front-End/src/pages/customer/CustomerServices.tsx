import { useState, useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import {
  Search,
  Zap,
  Wrench,
  Wind,
  Sparkles,
  Paintbrush,
  Trees,
  Hammer,
  Clock,
  ArrowRight,
  SlidersHorizontal,
  X,
  Layers
} from 'lucide-react'
import { catalogApi } from '@/api/catalog'
import { Pagination } from '@/components/shared/Pagination'
import { LoadingState, ErrorState, NoResultsState, EmptyState } from '@/components/shared/UxStates'
import { formatPrice } from '@/utils/formatters'
import { useDebounce } from '@/hooks/useDebounce'

interface CategoryMeta {
  name: string
  label: string
  icon: React.ElementType
}

const CATEGORIES: CategoryMeta[] = [
  { name: '', label: 'All Services', icon: Layers },
  { name: 'Electrical', label: 'Electrical', icon: Zap },
  { name: 'Plumbing', label: 'Plumbing', icon: Wrench },
  { name: 'HVAC', label: 'HVAC & AC', icon: Wind },
  { name: 'Cleaning', label: 'Cleaning', icon: Sparkles },
  { name: 'Painting', label: 'Painting', icon: Paintbrush },
  { name: 'Landscaping', label: 'Landscaping', icon: Trees },
  { name: 'Carpentry', label: 'Carpentry', icon: Hammer },
]

export default function CustomerServices() {
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [sortBy, setSortBy] = useState<'price_asc' | 'price_desc' | 'name'>('name')
  const [page, setPage] = useState(0)
  const debouncedSearch = useDebounce(search, 300)

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

  return (
    <div className="space-y-6 max-w-6xl">
      {/* Header Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-blue-700 via-indigo-700 to-slate-900 p-6 sm:p-8 text-white shadow-xl">
        <div className="absolute top-0 right-0 -mt-8 -mr-8 w-64 h-64 rounded-full bg-white/5 blur-2xl pointer-events-none" />
        <div className="relative z-10 max-w-2xl space-y-2">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-white/15 text-blue-100 backdrop-blur-md">
            <Sparkles className="w-3.5 h-3.5 text-amber-300" />
            Verified Marketplace Catalog
          </span>
          <h1 className="text-2xl sm:text-3xl font-black tracking-tight text-white">
            Explore Professional Services
          </h1>
          <p className="text-xs sm:text-sm text-blue-100/80 leading-relaxed">
            Browse fixed-price standard services offered by background-checked home service specialists.
          </p>
        </div>
      </div>

      {/* Category Pills Bar */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 no-scrollbar">
        {CATEGORIES.map((cat) => {
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
              className={`inline-flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs sm:text-sm font-semibold whitespace-nowrap transition-all ${
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
            placeholder="Search service name, description, or keyword…"
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
        <LoadingState message="Fetching catalog services…" />
      ) : error ? (
        <ErrorState
          message="Failed to load catalog services."
          action={{ label: 'Retry', onClick: () => refetch() }}
        />
      ) : items.length === 0 ? (
        debouncedSearch || category ? (
          <NoResultsState
            message={`No services found matching your criteria.`}
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
            message="Check back shortly as providers list additional specialized offerings."
          />
        )
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
            {items.map((item) => (
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

                  <h3 className="text-base font-bold text-slate-900 group-hover:text-blue-600 transition-colors line-clamp-1">
                    {item.name}
                  </h3>

                  {item.description ? (
                    <p className="text-xs text-slate-500 line-clamp-2 mt-2 leading-relaxed">
                      {item.description}
                    </p>
                  ) : (
                    <p className="text-xs text-slate-400 italic mt-2">
                      Professional service backed by ServiceConnect satisfaction guarantee.
                    </p>
                  )}

                  {item.durationMinutes && (
                    <div className="flex items-center gap-1.5 mt-3 text-xs text-slate-500 font-medium">
                      <Clock className="w-3.5 h-3.5 text-slate-400" />
                      <span>Estimated duration: {item.durationMinutes} minutes</span>
                    </div>
                  )}
                </div>

                <div className="mt-5 pt-4 border-t border-slate-100 flex items-center justify-between gap-3">
                  <div className="text-[11px] text-slate-400">
                    Provider ID: #{item.providerId}
                  </div>
                  <Link
                    to={`/customer/providers/${item.providerId}`}
                    className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-slate-900 hover:bg-blue-600 text-white text-xs font-bold transition-all shadow-sm group-hover:bg-blue-600"
                  >
                    <span>View Provider</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Link>
                </div>
              </div>
            ))}
          </div>

          {data && data.totalPages > 1 && (
            <div className="pt-2">
              <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />
            </div>
          )}
        </>
      )}
    </div>
  )
}
