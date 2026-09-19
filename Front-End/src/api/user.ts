import { userServiceClient } from '@/lib/axios'
import type {
  UserProfile,
  UpdateProfileRequest,
  UserSettings,
  OnboardingStatus,
} from '@/types'

// ============================================================
// User Service API
//
// Controller path: /api/users/**
// Gateway routes:  /api/v1/users/**
//
// IMPORTANT: Gateway path rewriting from /api/v1/users/** to
// /api/users/** is UNCONFIRMED. Using direct user service until
// rewriting behavior is verified against backend source.
//
// To switch to gateway: replace userServiceClient with gatewayClient
// and update BASE to '/api/v1/users'
// ============================================================
const BASE = '/api/users'

export const userApi = {
  /** POST /api/users — Create profile for authenticated user (called after registration) */
  createProfile: (data: { firstName: string; lastName: string; phone?: string }) =>
    userServiceClient.post<UserProfile>(BASE, data),

  /** GET /api/users/me */
  getMyProfile: () =>
    userServiceClient.get<UserProfile>(`${BASE}/me`),

  /** PUT /api/users/me */
  updateMyProfile: (data: UpdateProfileRequest) =>
    userServiceClient.put<UserProfile>(`${BASE}/me`, data),

  /** GET /api/users/me/onboarding */
  getOnboardingStatus: () =>
    userServiceClient.get<OnboardingStatus>(`${BASE}/me/onboarding`),

  /** GET /api/users/me/settings */
  getSettings: () =>
    userServiceClient.get<UserSettings>(`${BASE}/me/settings`),

  /** PUT /api/users/me/settings */
  updateSettings: (data: UserSettings) =>
    userServiceClient.put<UserSettings>(`${BASE}/me/settings`, data),

  /** GET /api/users — All user profiles (ADMIN) */
  getAll: () =>
    userServiceClient.get<UserProfile[]>(BASE),

  /** GET /api/users/:id */
  getById: (id: number) =>
    userServiceClient.get<UserProfile>(`${BASE}/${id}`),
}
