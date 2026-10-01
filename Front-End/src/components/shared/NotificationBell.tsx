import { useState, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Bell,
  CheckCheck,
  MessageSquare,
  FileText,
  CalendarCheck,
  Star,
  Headphones,
  Shield,
  Clock,
  Sparkles,
  ExternalLink,
  Mail,
} from 'lucide-react'
import { notificationApi } from '@/api/notifications'
import { pushNotifications } from '@/lib/pushNotifications'
import type { NotificationItem, NotificationType } from '@/types'

function formatRelativeTime(dateString: string): string {
  try {
    const diff = Math.floor((Date.now() - new Date(dateString).getTime()) / 1000)
    if (isNaN(diff) || diff < 0) return 'Just now'
    if (diff < 60) return 'Just now'
    if (diff < 3600) return `${Math.floor(diff / 60)}m ago`
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`
    if (diff < 604800) return `${Math.floor(diff / 86400)}d ago`
    return new Date(dateString).toLocaleDateString()
  } catch {
    return 'Recently'
  }
}

function getNotificationIcon(type: NotificationType) {
  switch (type) {
    case 'CHAT_MESSAGE':
      return { icon: MessageSquare, bg: 'bg-blue-100 text-blue-600' }
    case 'QUOTE_CREATED':
    case 'QUOTE_ACCEPTED':
    case 'QUOTE_DECLINED':
    case 'QUOTE_EXPIRED':
      return { icon: FileText, bg: 'bg-amber-100 text-amber-600' }
    case 'BOOKING_CREATED':
    case 'BOOKING_ACCEPTED':
    case 'BOOKING_DECLINED':
    case 'BOOKING_CANCELLED':
    case 'BOOKING_STATUS_CHANGED':
    case 'BOOKING_COMPLETED':
    case 'BOOKING_REMINDER':
      return { icon: CalendarCheck, bg: 'bg-indigo-100 text-indigo-600' }
    case 'REVIEW_RECEIVED':
    case 'REVIEW_REMINDER':
      return { icon: Star, bg: 'bg-yellow-100 text-yellow-600' }
    case 'SUPPORT_MESSAGE':
    case 'SUPPORT_REPLY':
      return { icon: Headphones, bg: 'bg-emerald-100 text-emerald-600' }
    case 'NEW_LOGIN':
    case 'PASSWORD_CHANGED':
    case 'SECURITY_ALERT':
      return { icon: Shield, bg: 'bg-rose-100 text-rose-600' }
    default:
      return { icon: Bell, bg: 'bg-slate-100 text-slate-600' }
  }
}

export function NotificationBell() {
  const [isOpen, setIsOpen] = useState(false)
  const [permission, setPermission] = useState<NotificationPermission>(
    pushNotifications.getPermission()
  )
  const dropdownRef = useRef<HTMLDivElement>(null)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  // Register service worker on mount
  useEffect(() => {
    pushNotifications.registerServiceWorker()
  }, [])

  // Close dropdown on outside click
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false)
      }
    }
    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside)
    }
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [isOpen])

  // 1. Authoritative unread count from PostgreSQL
  const { data: unreadData } = useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn: async () => {
      const res = await notificationApi.getUnreadCount()
      return res.data
    },
    refetchInterval: 10000, // Poll every 10 seconds for real updates
  })

  const unreadCount = unreadData?.unreadCount ?? 0

  // 2. Authoritative notification list from PostgreSQL
  const { data: notifications = [], isLoading, refetch } = useQuery({
    queryKey: ['notifications', 'list'],
    queryFn: async () => {
      const res = await notificationApi.getNotifications(0, 30)
      return res.data
    },
    enabled: isOpen, // fetch on dropdown open
  })

  // Mark single as read
  const markReadMutation = useMutation({
    mutationFn: (id: number) => notificationApi.markAsRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications', 'unread-count'] })
      queryClient.invalidateQueries({ queryKey: ['notifications', 'list'] })
    },
  })

  // Mark all as read
  const markAllReadMutation = useMutation({
    mutationFn: () => notificationApi.markAllAsRead(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications', 'unread-count'] })
      queryClient.invalidateQueries({ queryKey: ['notifications', 'list'] })
    },
  })

  const handleToggle = () => {
    const nextState = !isOpen
    setIsOpen(nextState)
    if (nextState) {
      refetch()
    }
  }

  const handleNotificationClick = (item: NotificationItem) => {
    if (!item.read) {
      markReadMutation.mutate(item.id)
    }
    setIsOpen(false)
    if (item.deepLink) {
      navigate(item.deepLink)
    }
  }

  const handleEnablePush = async () => {
    const res = await pushNotifications.requestPermission()
    setPermission(res)
  }

  return (
    <div className="relative" ref={dropdownRef}>
      {/* Bell Button */}
      <button
        onClick={handleToggle}
        className="relative p-2 rounded-xl text-slate-500 hover:text-slate-900 hover:bg-slate-100 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500/20"
        aria-label="View notifications"
        title="Notifications"
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <span className="absolute top-1 right-1 min-w-[18px] h-[18px] px-1 bg-blue-600 text-white text-[10px] font-bold rounded-full flex items-center justify-center border-2 border-white shadow-sm animate-pulse">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown Panel */}
      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white rounded-2xl shadow-2xl border border-slate-200/80 z-50 overflow-hidden animate-in fade-in duration-150">
          {/* Header */}
          <div className="px-4 py-3.5 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
            <div className="flex items-center gap-2">
              <span className="text-sm font-bold text-slate-900">Notifications</span>
              {unreadCount > 0 && (
                <span className="px-2 py-0.5 rounded-full text-[11px] font-bold bg-blue-100 text-blue-700">
                  {unreadCount} new
                </span>
              )}
            </div>

            {unreadCount > 0 && (
              <button
                onClick={() => markAllReadMutation.mutate()}
                disabled={markAllReadMutation.isPending}
                className="text-xs font-semibold text-blue-600 hover:text-blue-700 transition-colors flex items-center gap-1 disabled:opacity-50"
              >
                <CheckCheck className="w-3.5 h-3.5" />
                Mark all read
              </button>
            )}
          </div>

          {/* Browser Push Permission Banner (if not yet granted) */}
          {permission === 'default' && (
            <div className="p-3 bg-blue-50/70 border-b border-blue-100 flex items-center justify-between gap-2">
              <div className="flex items-center gap-2 text-blue-800 text-xs">
                <Sparkles className="w-4 h-4 text-blue-600 flex-shrink-0" />
                <span>Enable browser push to receive instant updates</span>
              </div>
              <button
                onClick={handleEnablePush}
                className="px-2.5 py-1 text-[11px] font-bold bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors whitespace-nowrap shadow-xs"
              >
                Enable
              </button>
            </div>
          )}

          {/* List Content */}
          <div className="max-h-[380px] overflow-y-auto divide-y divide-slate-100/80">
            {isLoading ? (
              <div className="p-6 text-center text-xs text-slate-400">Loading notifications…</div>
            ) : notifications.length === 0 ? (
              <div className="p-8 text-center">
                <div className="w-10 h-10 bg-slate-100 rounded-full flex items-center justify-center text-slate-400 mx-auto mb-2">
                  <Bell className="w-5 h-5" />
                </div>
                <p className="text-xs font-semibold text-slate-700">No notifications yet.</p>
                <p className="text-[11px] text-slate-400 mt-0.5">
                  You'll be notified when there is activity on your bookings, quotes, or messages.
                </p>
              </div>
            ) : (
              notifications.map((item) => {
                const { icon: Icon, bg } = getNotificationIcon(item.type)
                return (
                  <button
                    key={item.id}
                    onClick={() => handleNotificationClick(item)}
                    className={`w-full p-3.5 text-left flex items-start gap-3 hover:bg-slate-50/80 transition-colors relative group ${
                      !item.read ? 'bg-blue-50/25' : 'bg-white'
                    }`}
                  >
                    {/* Event Type Icon */}
                    <div className={`w-8 h-8 rounded-xl flex items-center justify-center flex-shrink-0 ${bg}`}>
                      <Icon className="w-4 h-4" />
                    </div>

                    {/* Content */}
                    <div className="flex-1 min-w-0 pr-4">
                      <div className="flex items-center justify-between gap-1 mb-0.5">
                        <p className={`text-xs font-bold truncate ${!item.read ? 'text-slate-900' : 'text-slate-700'}`}>
                          {item.title}
                        </p>
                      </div>
                      <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed">
                        {item.message}
                      </p>
                      <div className="flex items-center gap-2 mt-1.5 text-[10px] text-slate-400 font-medium">
                        <span className="flex items-center gap-1">
                          <Clock className="w-3 h-3" />
                          <span>{formatRelativeTime(item.createdAt)}</span>
                        </span>
                        {item.emailSent && (
                          <span className="flex items-center gap-0.5 text-blue-600 bg-blue-50 px-1.5 py-0.5 rounded text-[9px] font-semibold" title={`Delivered to email: ${item.emailRecipient || 'registered email'}`}>
                            <Mail className="w-2.5 h-2.5" />
                            <span>Email</span>
                          </span>
                        )}
                        {item.deepLink && (
                          <span className="opacity-0 group-hover:opacity-100 transition-opacity text-blue-600 flex items-center gap-0.5 ml-auto">
                            <span>Open</span>
                            <ExternalLink className="w-2.5 h-2.5" />
                          </span>
                        )}
                      </div>
                    </div>

                    {/* Unread Indicator Dot */}
                    {!item.read && (
                      <span className="absolute top-4 right-3.5 w-2 h-2 rounded-full bg-blue-600 shadow-xs" />
                    )}
                  </button>
                )
              })
            )}
          </div>

          {/* Footer */}
          <div className="px-4 py-2 border-t border-slate-100 bg-slate-50/50 text-center">
            <span className="text-[10px] text-slate-400">
              ServiceConnect Real-Time Notifications
            </span>
          </div>
        </div>
      )}
    </div>
  )
}
