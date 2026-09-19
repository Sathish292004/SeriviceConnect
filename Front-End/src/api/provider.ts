import { providerClient, gatewayClient } from '@/lib/axios'
import type {
  Provider,
  ProviderPublicView,
  CreateProviderRequest,
  UpdateProviderRequest,
  UpdateLocationRequest,
  UpdateProviderStatusRequest,
  ProviderAvailability,
  CreateAvailabilityRequest,
  UpdateAvailabilityRequest,
  ProviderPhoto,
  AddPhotoRequest,
  PageResponse,
  PaginationParams,
  ProviderStatus,
} from '@/types'

// ============================================================
// Provider API
//
// ROUTING RULES (per spec §6):
//
// Customer discovery (unauthenticated or CUSTOMER role):
//   → providerClient (direct to port 8084)
//   Reason: Gateway SecurityConfig restricts /api/v1/providers/**
//   to PROVIDER + ADMIN roles. Customer discovery MUST bypass gateway.
//
// Provider/Admin authenticated operations:
//   → gatewayClient (through gateway /api/v1/providers/**)
//   Reason: These require PROVIDER or ADMIN JWT — gateway allows them.
//
// All paths are /api/v1/providers/** (provider-service controller uses this prefix).
// ============================================================

const BASE = '/api/v1/providers'

// ---- Customer-facing (direct provider service) --------------

export const providerDiscoveryApi = {
  /** GET /api/v1/providers — All approved providers (paginated) */
  getAll: (params?: PaginationParams & { status?: ProviderStatus }) =>
    providerClient.get<PageResponse<ProviderPublicView>>(BASE, { params }),

  /** GET /api/v1/providers/:id/public — Public provider view (CUSTOMER) */
  getPublic: (providerId: number) =>
    providerClient.get<ProviderPublicView>(`${BASE}/${providerId}/public`),

  /** GET /api/v1/providers/:id/availability/active — Active availability slots */
  getActiveAvailability: (providerId: number) =>
    providerClient.get<ProviderAvailability[]>(`${BASE}/${providerId}/availability/active`),

  /** GET /api/v1/providers/:id/photos */
  getPhotos: (providerId: number) =>
    providerClient.get<ProviderPhoto[]>(`${BASE}/${providerId}/photos`),
}

// ---- Provider-authenticated (through gateway) ---------------

export const providerApi = {
  /** POST /api/v1/providers — Create own provider profile (PROVIDER role) */
  create: (data: CreateProviderRequest) =>
    gatewayClient.post<Provider>(BASE, data),

  /** GET /api/v1/providers/me — Own provider profile */
  getMe: () =>
    gatewayClient.get<Provider>(`${BASE}/me`),

  /** GET /api/v1/providers/onboarding/status */
  getOnboardingStatus: () =>
    gatewayClient.get(`${BASE}/onboarding/status`),

  /** PUT /api/v1/providers/:id — Update own profile */
  update: (providerId: number, data: UpdateProviderRequest) =>
    gatewayClient.put<Provider>(`${BASE}/${providerId}`, data),

  /** PATCH /api/v1/providers/:id/location — Update live GPS location */
  updateLocation: (providerId: number, data: UpdateLocationRequest) =>
    gatewayClient.patch<Provider>(`${BASE}/${providerId}/location`, data),

  // ---- Availability -------------------------------------------

  /** POST /api/v1/providers/:id/availability */
  createAvailability: (providerId: number, data: CreateAvailabilityRequest) =>
    gatewayClient.post<ProviderAvailability>(`${BASE}/${providerId}/availability`, data),

  /** GET /api/v1/providers/:id/availability */
  getMyAvailability: (providerId: number) =>
    gatewayClient.get<ProviderAvailability[]>(`${BASE}/${providerId}/availability`),

  /** PUT /api/v1/providers/:id/availability/:availabilityId */
  updateAvailability: (providerId: number, availabilityId: number, data: UpdateAvailabilityRequest) =>
    gatewayClient.put<ProviderAvailability>(`${BASE}/${providerId}/availability/${availabilityId}`, data),

  /** DELETE /api/v1/providers/:id/availability/:availabilityId */
  deleteAvailability: (providerId: number, availabilityId: number) =>
    gatewayClient.delete(`${BASE}/${providerId}/availability/${availabilityId}`),

  /** PATCH /api/v1/providers/:id/availability/:availabilityId/active */
  toggleAvailability: (providerId: number, availabilityId: number, active: boolean) =>
    gatewayClient.patch(`${BASE}/${providerId}/availability/${availabilityId}/active`, { active }),

  // ---- Photos -------------------------------------------------

  /** POST /api/v1/providers/:id/photos */
  addPhoto: (providerId: number, data: AddPhotoRequest) =>
    gatewayClient.post<ProviderPhoto>(`${BASE}/${providerId}/photos`, data),

  /** POST /api/v1/providers/:id/photos/upload — Direct multipart file upload */
  uploadPhoto: (providerId: number, file: File, displayOrder?: number) => {
    const formData = new FormData()
    formData.append('file', file)
    if (displayOrder !== undefined) {
      formData.append('displayOrder', String(displayOrder))
    }
    return gatewayClient.post<ProviderPhoto>(`${BASE}/${providerId}/photos/upload`, formData)
  },

  /** GET /api/v1/providers/:id/photos (provider view — authenticated) */
  getMyPhotos: (providerId: number) =>
    gatewayClient.get<ProviderPhoto[]>(`${BASE}/${providerId}/photos`),

  /** DELETE /api/v1/providers/:id/photos/:photoId */
  deletePhoto: (providerId: number, photoId: number) =>
    gatewayClient.delete(`${BASE}/${providerId}/photos/${photoId}`),
}

// ---- Admin provider management (through gateway) ------------

export const adminProviderApi = {
  /** GET /api/v1/providers/admin/all — All providers paginated */
  getAll: (params?: PaginationParams & { status?: ProviderStatus }) =>
    gatewayClient.get<PageResponse<Provider>>(`${BASE}/admin/all`, { params }),

  /** GET /api/v1/providers/:id — Provider by ID */
  getById: (providerId: number) =>
    gatewayClient.get<Provider>(`${BASE}/${providerId}`),

  /** PATCH /api/v1/providers/:id/status */
  updateStatus: (providerId: number, data: UpdateProviderStatusRequest) =>
    gatewayClient.patch(`${BASE}/${providerId}/status`, data),

  /** DELETE /api/v1/providers/:id */
  delete: (providerId: number) =>
    gatewayClient.delete(`${BASE}/${providerId}`),
}
