interface AvatarProps {
  name?: string
  src?: string
  size?: 'sm' | 'md' | 'lg' | 'xl'
  status?: 'online' | 'offline' | 'busy'
  className?: string
}

const SIZES = {
  sm: 'w-8 h-8 text-xs',
  md: 'w-10 h-10 text-sm',
  lg: 'w-14 h-14 text-base font-semibold',
  xl: 'w-20 h-20 text-xl font-bold',
}

const STATUS_SIZES = {
  sm: 'w-2 h-2 border',
  md: 'w-2.5 h-2.5 border-2',
  lg: 'w-3.5 h-3.5 border-2',
  xl: 'w-4 h-4 border-2',
}

const STATUS_COLORS = {
  online: 'bg-emerald-500',
  offline: 'bg-slate-400',
  busy: 'bg-amber-500',
}

export function Avatar({
  name = 'User',
  src,
  size = 'md',
  status,
  className = '',
}: AvatarProps) {
  const getInitials = (str: string) => {
    const parts = str.trim().split(/\s+/)
    if (parts.length >= 2) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase()
    }
    return str.slice(0, 2).toUpperCase()
  }

  // Consistent background color from name
  const getColor = (str: string) => {
    const colors = [
      'bg-blue-100 text-blue-700',
      'bg-indigo-100 text-indigo-700',
      'bg-violet-100 text-violet-700',
      'bg-emerald-100 text-emerald-700',
      'bg-amber-100 text-amber-700',
      'bg-teal-100 text-teal-700',
    ]
    let hash = 0
    for (let i = 0; i < str.length; i++) {
      hash = str.charCodeAt(i) + ((hash << 5) - hash)
    }
    return colors[Math.abs(hash) % colors.length]
  }

  return (
    <div className={`relative inline-flex flex-shrink-0 items-center justify-center rounded-full overflow-hidden ${SIZES[size]} ${className}`}>
      {src ? (
        <img
          src={src}
          alt={name}
          className="w-full h-full object-cover"
          onError={(e) => {
            ;(e.target as HTMLElement).style.display = 'none'
          }}
        />
      ) : (
        <div className={`w-full h-full flex items-center justify-center font-medium ${getColor(name)}`}>
          {getInitials(name)}
        </div>
      )}

      {status && (
        <span
          className={`absolute bottom-0 right-0 rounded-full border-white ${STATUS_SIZES[size]} ${STATUS_COLORS[status]}`}
        />
      )}
    </div>
  )
}
