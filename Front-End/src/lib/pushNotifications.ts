import { notificationApi } from '@/api/notifications'

// Standard Base64 url to Uint8Array converter for VAPID applicationServerKey
function urlBase64ToUint8Array(base64String: string): Uint8Array {
  const padding = '='.repeat((4 - (base64String.length % 4)) % 4)
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/')
  const rawData = window.atob(base64)
  const outputArray = new Uint8Array(rawData.length)
  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i)
  }
  return outputArray
}

// Default standard dummy VAPID public key for web push subscription negotiation
const VAPID_PUBLIC_KEY =
  'BEl62iUYgUivxIkv69yViEuiBIa-Ib9-SkvMeAtA3LFgDzkrxZJjSgSnfckjBJuBKr3qBUYIHBQFLXYp5Nksh8U'

export const pushNotifications = {
  /** Check if browser supports notifications and service worker */
  isSupported(): boolean {
    return typeof window !== 'undefined' && 'Notification' in window && 'serviceWorker' in navigator
  },

  /** Get current browser permission state */
  getPermission(): NotificationPermission {
    if (!this.isSupported()) return 'denied'
    return Notification.permission
  },

  /** Register service worker sw.js */
  async registerServiceWorker(): Promise<ServiceWorkerRegistration | null> {
    if (!this.isSupported()) return null
    try {
      const reg = await navigator.serviceWorker.register('/sw.js', { scope: '/' })
      return reg
    } catch (err) {
      console.warn('Service worker registration failed:', err)
      return null
    }
  },

  /** Request user permission for notifications */
  async requestPermission(): Promise<NotificationPermission> {
    if (!this.isSupported()) return 'denied'
    try {
      const permission = await Notification.requestPermission()
      if (permission === 'granted') {
        await this.subscribeToPush()
      }
      return permission
    } catch (err) {
      console.warn('Error requesting notification permission:', err)
      return 'denied'
    }
  },

  /** Subscribe to push manager and persist to backend PostgreSQL */
  async subscribeToPush(): Promise<boolean> {
    if (!this.isSupported()) return false
    try {
      const reg = await this.registerServiceWorker()
      if (!reg) return false

      let sub = await reg.pushManager.getSubscription()
      if (!sub) {
        const appServerKey = urlBase64ToUint8Array(VAPID_PUBLIC_KEY)
        sub = await reg.pushManager.subscribe({
          userVisibleOnly: true,
          applicationServerKey: appServerKey as unknown as BufferSource,
        })
      }

      if (sub) {
        const rawKey = sub.getKey('p256dh')
        const rawAuth = sub.getKey('auth')
        const p256dh = rawKey ? btoa(String.fromCharCode.apply(null, Array.from(new Uint8Array(rawKey)))) : ''
        const auth = rawAuth ? btoa(String.fromCharCode.apply(null, Array.from(new Uint8Array(rawAuth)))) : ''

        await notificationApi.registerPushSubscription({
          endpoint: sub.endpoint,
          keys: { p256dh, auth },
          userAgent: navigator.userAgent,
        })
        return true
      }
      return false
    } catch (err) {
      console.warn('Failed to subscribe to Web Push:', err)
      return false
    }
  },

  /** Display local browser notification when tab is inactive / hidden */
  showIfHidden(title: string, message: string, deepLink?: string, id?: number) {
    if (!this.isSupported() || Notification.permission !== 'granted') return
    if (typeof document !== 'undefined' && !document.hidden) {
      // If user is actively looking at tab, in-app notification center / toast is primary
      return
    }

    try {
      const notif = new Notification(title, {
        body: message,
        icon: '/favicon.svg',
        tag: 'serviceconnect-' + (id || Date.now()),
        data: { url: deepLink || '/' },
      })
      notif.onclick = () => {
        window.focus()
        if (deepLink) {
          window.location.href = deepLink
        }
        notif.close()
      }
    } catch {
      // Fallback to service worker showNotification
      navigator.serviceWorker?.ready.then((reg) => {
        reg.showNotification(title, {
          body: message,
          icon: '/favicon.svg',
          data: { url: deepLink || '/' },
        })
      })
    }
  },
}
