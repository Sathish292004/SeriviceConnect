import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Search } from 'lucide-react'
import { catalogApi } from '@/api/catalog'
import { Pagination } from '@/components/shared/Pagination'
import { LoadingState, ErrorState, NoResultsState, EmptyState } from '@/components/shared/UxStates'
import { formatPrice } from '@/utils/formatters'
import { useDebounce } from '@/hooks/useDebounce'

import { MAJOR_CATEGORIES } from '@/lib/categories'

export default function Services() {
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [page, setPage] = useState(0)
  const debouncedSearch = useDebounce(search, 300)

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['catalog', 'public', debouncedSearch, category, page],
    queryFn: () => catalogApi.search({ search: debouncedSearch || undefined, category: category || undefined, page, size: 12 }),
    select: (res) => res.data,
  })

  const items = data?.content ?? []

  return (
    <div className="page-container py-8 space-y-6">
      <h1 className="text-2xl font-bold text-[#0F172A]">Browse Services</h1>
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#94A3B8]" />
          <input value={search} onChange={(e) => { setSearch(e.target.value); setPage(0) }} placeholder="Search services…" className="sc-input pl-10" />
        </div>
        <select value={category} onChange={(e) => { setCategory(e.target.value); setPage(0) }} className="sc-input w-auto min-w-[180px]" aria-label="Category">
          {MAJOR_CATEGORIES.map((cat) => (
            <option key={cat.name} value={cat.name}>
              {cat.label}
            </option>
          ))}
        </select>
      </div>

      {isLoading ? <LoadingState message="Loading services…" /> : error ? (
        <ErrorState message="Failed to load services." action={{ label: 'Retry', onClick: () => refetch() }} />
      ) : items.length === 0 ? (
        debouncedSearch || category
          ? <NoResultsState message={`No services found${debouncedSearch ? ` for "${debouncedSearch}"` : ''}.`} action={{ label: 'Clear', onClick: () => { setSearch(''); setCategory('') } }} />
          : <EmptyState title="No services available" message="Check back later." />
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {items.map((item) => (
              <div key={item.id} className="sc-card-hover p-5">
                <div className="flex items-start justify-between mb-2">
                  <h3 className="text-sm font-semibold text-[#0F172A]">{item.name}</h3>
                  <p className="text-base font-bold text-[#0F172A]">{formatPrice(item.price)}</p>
                </div>
                <span className="tag-pill">{item.category}</span>
                {item.description && <p className="text-xs text-[#64748B] line-clamp-2 mt-2">{item.description}</p>}
                <div className="flex gap-2 mt-4">
                  <Link to={`/providers?q=${encodeURIComponent(item.name)}`} className="sc-btn-outline flex-1 text-xs text-center">Find Nearby</Link>
                  <Link to={`/providers/${item.providerId}`} className="sc-btn-primary flex-1 text-xs text-center">View Provider</Link>
                </div>
              </div>
            ))}
          </div>
          <Pagination page={page} totalPages={data?.totalPages ?? 1} onPageChange={setPage} />
        </>
      )}
    </div>
  )
}
