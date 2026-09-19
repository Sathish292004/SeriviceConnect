import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/store/authStore'
import type { RefreshResponse } from '@/types'

// ============================================================
// ServiceConnect — Centralized Axios Clients
//
// Per spec §10 + §6:
//   gatewayClient     — auth, bookings, catalog (via gateway rewrites)
//   userServiceClient — user profiles (direct: gateway rewrite unconfirmed)
//   providerClient    — provider discovery (direct: gateway restricts to PROVIDER/ADMIN)
//   adminServiceClient— tickets + admin APIs (direct: no gateway route for tickets)
//   paymentClient     — payments (direct: no gateway route for payments)
// ============================================================

const GATEWAY_URL = ''
const USER_SERVICE_URL = ''
const PROVIDER_SERVICE_URL = ''
const ADMIN_SERVICE_URL = import.meta.env.VITE_ADMIN_SERVICE_URL ?? 'http://localhost:8083'
const PAYMENT_SERVICE_URL = ''

// ---- Token refresh state ------------------------------------
let isRefreshing = false
let refreshSubscribers: Array<(token: string) => void> = []

function subscribeTokenRefresh(cb: (token: string) => void) {
  refreshSubscribers.push(cb)
}

function onRefreshSuccess(newToken: string) {
  refreshSubscribers.forEach((cb) => cb(newToken))
  refreshSubscribers = []
}

function onRefreshFailure() {
  refreshSubscribers = []
  const { clearAuth, isAuthenticated } = useAuthStore.getState()
  clearAuth()
  // Navigate to session expired only if user was actually logged in
  if (isAuthenticated &&
      !window.location.pathname.includes('/login') &&
      !window.location.pathname.includes('/session-expired')) {
    window.location.href = '/session-expired'
  }
}

// ---- Create base client -------------------------------------
function createClient(baseURL: string, timeout = 15000): AxiosInstance {
  return axios.create({
    baseURL,
    timeout,
    headers: {
      'Content-Type': 'application/json',
    },
  })
}

// ---- Request interceptor: attach JWT ------------------------
function attachAuthInterceptor(client: AxiosInstance): void {
  client.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      const { accessToken } = useAuthStore.getState()
      if (accessToken && config.headers) {
        // Never log the token — only attach it
        config.headers['Authorization'] = `Bearer ${accessToken}`
      }
      if (config.data instanceof FormData && config.headers) {
        delete config.headers['Content-Type']
      }
      return config
    },
    (error) => Promise.reject(error),
  )
}

// ---- Response interceptor: 401 → refresh → retry once ------
function attachRefreshInterceptor(client: AxiosInstance): void {
  client.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

      if (error.response?.status === 401 && !originalRequest._retry) {
        const { refreshToken, isAuthenticated } = useAuthStore.getState()

        // No refresh token — log out only if user was authenticated
        if (!refreshToken) {
          if (isAuthenticated) {
            onRefreshFailure()
          }
          return Promise.reject(error)
        }

        // Already refreshing — queue this request
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            subscribeTokenRefresh((newToken: string) => {
              if (originalRequest.headers) {
                originalRequest.headers['Authorization'] = `Bearer ${newToken}`
              }
              resolve(client(originalRequest))
            })
            // If refresh fails, reject all queued
            setTimeout(() => reject(error), 30000)
          })
        }

        originalRequest._retry = true
        isRefreshing = true

        try {
          // Call refresh endpoint directly (not through intercepted client to avoid loop)
          const resp = await axios.post<RefreshResponse>(
            `${GATEWAY_URL}/api/v1/auth/refresh`,
            { refreshToken },
            { timeout: 10000 },
          )

          const { accessToken: newAccessToken, refreshToken: newRefreshToken } = resp.data
          const { setTokens } = useAuthStore.getState()
          setTokens(newAccessToken, newRefreshToken)

          // Update header on original request
          if (originalRequest.headers) {
            originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`
          }

          onRefreshSuccess(newAccessToken)
          isRefreshing = false

          return client(originalRequest)
        } catch {
          isRefreshing = false
          onRefreshFailure()
          return Promise.reject(error)
        }
      }

      return Promise.reject(error)
    },
  )
}

// ---- Instantiate clients ------------------------------------

/** Gateway client — auth, bookings, catalog (via /api/v1/catalog/**), reviews (via /api/v1/reviews/**), help center, admin APIs */
export const gatewayClient = createClient(GATEWAY_URL)
attachAuthInterceptor(gatewayClient)
attachRefreshInterceptor(gatewayClient)

/**
 * User service client (direct)
 *
 * Gateway routes /api/v1/users/** → controller at /api/users/**
 * Until Gateway path rewriting is confirmed, use direct service.
 * NOTE: Gateway rewrite behavior must be verified before switching to gateway.
 */
export const userServiceClient = createClient(USER_SERVICE_URL)
attachAuthInterceptor(userServiceClient)
attachRefreshInterceptor(userServiceClient)

/**
 * Provider client (direct)
 *
 * Customer-facing provider discovery endpoints:
 *   GET /api/v1/providers (paginated)
 *   GET /api/v1/providers/:id/public
 *   GET /api/v1/providers/:id/availability/active
 *   GET /api/v1/providers/:id/photos
 *
 * Gateway SecurityConfig restricts /api/v1/providers/** to PROVIDER + ADMIN.
 * Therefore customer discovery MUST use direct service.
 */
export const providerClient = createClient(PROVIDER_SERVICE_URL)
attachAuthInterceptor(providerClient)
attachRefreshInterceptor(providerClient)

/**
 * Admin service client (direct)
 *
 * Tickets: /api/v1/tickets/**
 * Gateway has NO route for tickets — use direct admin service.
 *
 * Also handles:
 * - Help center admin APIs
 * - Audit logs
 * - Admin provider operations
 */
export const adminServiceClient = createClient(ADMIN_SERVICE_URL)
attachAuthInterceptor(adminServiceClient)
attachRefreshInterceptor(adminServiceClient)

/**
 * Payment client (direct)
 *
 * Gateway has NO route for /api/payments/**
 * Payment service is at port 8088.
 * IMPORTANT: Payment service uses UUID for bookingId/userId.
 * Auth/Booking use Long. This mismatch is handled in src/api/payment.ts.
 */
export const paymentClient = createClient(PAYMENT_SERVICE_URL)
attachAuthInterceptor(paymentClient)
attachRefreshInterceptor(paymentClient)

// ---- Error mapping ------------------------------------------
export interface MappedError {
  status: number
  message: string
  fieldErrors?: Record<string, string>
  isOffline: boolean
  isSessionExpired: boolean
  isRateLimited: boolean
  isForbidden: boolean
  isNotFound: boolean
  isConflict: boolean
  isValidation: boolean
  isServerError: boolean
  isServiceUnavailable: boolean
}

export function mapApiError(error: unknown): MappedError {
  const base: MappedError = {
    status: 0,
    message: 'An unexpected error occurred.',
    isOffline: false,
    isSessionExpired: false,
    isRateLimited: false,
    isForbidden: false,
    isNotFound: false,
    isConflict: false,
    isValidation: false,
    isServerError: false,
    isServiceUnavailable: false,
  }

  if (!navigator.onLine || (error instanceof AxiosError && !error.response)) {
    return { ...base, isOffline: true, message: 'No internet connection. Please check your network.' }
  }

  if (!(error instanceof AxiosError) || !error.response) {
    return { ...base, message: 'Network error. Please try again.' }
  }

  const { status, data } = error.response

  // Extract backend error message safely — never expose stack traces or internal service names
  const backendMessage: string =
    typeof data?.message === 'string'
      ? data.message
      : typeof data?.error === 'string'
      ? data.error
      : ''

  switch (status) {
    case 401:
      return { ...base, status, isSessionExpired: true, message: 'Your session has expired. Please log in again.' }
    case 403:
      return { ...base, status, isForbidden: true, message: 'You do not have permission to perform this action.' }
    case 404:
      return { ...base, status, isNotFound: true, message: backendMessage || 'The requested resource was not found.' }
    case 409:
      return { ...base, status, isConflict: true, message: backendMessage || 'A conflict occurred. Please refresh and try again.' }
    case 422:
      return {
        ...base,
        status,
        isValidation: true,
        message: backendMessage || 'Validation failed. Please check your input.',
        fieldErrors: data?.errors ?? {},
      }
    case 429:
      return { ...base, status, isRateLimited: true, message: 'Too many requests. Please wait a moment and try again.' }
    case 500:
      return { ...base, status, isServerError: true, message: 'A server error occurred. Please try again later.' }
    case 502:
    case 503:
    case 504:
      return { ...base, status, isServiceUnavailable: true, message: 'Service temporarily unavailable. Please try again later.' }
    default:
      return { ...base, status, message: backendMessage || 'Something went wrong. Please try again.' }
  }
}
