import { Search, X } from 'lucide-react'

interface SearchBarProps {
  value: string
  onChange: (value: string) => void
  placeholder?: string
  category?: string
  onCategoryChange?: (category: string) => void
  categories?: { label: string; value: string }[]
  className?: string
  size?: 'sm' | 'md' | 'lg'
}

export function SearchBar({
  value,
  onChange,
  placeholder = 'Search services or providers...',
  category,
  onCategoryChange,
  categories,
  className = '',
  size = 'md',
}: SearchBarProps) {
  const sizeClasses = {
    sm: 'py-2 text-xs',
    md: 'py-2.5 text-sm',
    lg: 'py-3.5 text-base',
  }

  const iconSizes = {
    sm: 'w-3.5 h-3.5 left-3',
    md: 'w-4 h-4 left-3.5',
    lg: 'w-5 h-5 left-4',
  }

  return (
    <div className={`flex flex-col sm:flex-row items-stretch gap-2.5 ${className}`}>
      <div className="relative flex-1">
        <Search className={`absolute top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none ${iconSizes[size]}`} />
        <input
          type="text"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          className={`w-full bg-white border border-slate-200 rounded-xl pl-10 pr-10 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600 transition-all text-slate-800 placeholder-slate-400 shadow-sm ${sizeClasses[size]}`}
        />
        {value && (
          <button
            type="button"
            onClick={() => onChange('')}
            className="absolute right-3 top-1/2 -translate-y-1/2 p-1 text-slate-400 hover:text-slate-600 rounded-full hover:bg-slate-100"
            aria-label="Clear search"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        )}
      </div>

      {categories && onCategoryChange && (
        <select
          value={category || ''}
          onChange={(e) => onCategoryChange(e.target.value)}
          className={`bg-white border border-slate-200 rounded-xl px-4 text-slate-700 font-medium focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600 shadow-sm transition-all cursor-pointer ${sizeClasses[size]}`}
          aria-label="Filter by category"
        >
          <option value="">All Categories</option>
          {categories.map((cat) => (
            <option key={cat.value} value={cat.value}>
              {cat.label}
            </option>
          ))}
        </select>
      )}
    </div>
  )
}
