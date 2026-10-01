import { gatewayClient } from '@/lib/axios'
import type {
  NotificationItem,
  UnreadCountResponse,
  PushSubscriptionRequest,
} from '@/types'

const BASE = '/api/v1/bookings/notifications'

export const notificationApi = {
  /** Get notifications for current user */
  getNotifications: (page = 0, size = 20) =>
    gatewayClient.get<NotificationItem[]>(`${BASE}?page=${page}&size=${size}`),

  /** Get authoritative unread count from PostgreSQL */
  getUnreadCount: () =>
    gatewayClient.get<UnreadCountResponse>(`${BASE}/unread-count`),

  /** Mark single notification as read in PostgreSQL */
  markAsRead: (id: number) =>
    gatewayClient.patch<NotificationItem>(`${BASE}/${id}/read`),

  /** Mark all notifications as read in PostgreSQL */
  markAllAsRead: () =>
    gatewayClient.patch<{ success: boolean; updatedCount: number }>(`${BASE}/read-all`),

  /** Register push subscription token in PostgreSQL */
  registerPushSubscription: (data: PushSubscriptionRequest) =>
    gatewayClient.post<{ success: boolean }>(`${BASE}/push-subscriptions`, data),

  /** Remove push subscription */
  removePushSubscription: (endpoint: string) =>
    gatewayClient.delete<{ success: boolean }>(`${BASE}/push-subscriptions?endpoint=${encodeURIComponent(endpoint)}`),

  /** Emit custom real event notification */
  emitEvent: (data: Record<string, unknown>) =>
    gatewayClient.post<NotificationItem>(`${BASE}/events`, data),

  /** Get notification preferences from PostgreSQL */
  getPreferences: () =>
    gatewayClient.get<Record<string, unknown>>(`${BASE}/preferences`),

  /** Update notification preferences (including email notifications) */
  updatePreferences: (data: Record<string, unknown>) =>
    gatewayClient.put<Record<string, unknown>>(`${BASE}/preferences`, data),
}
