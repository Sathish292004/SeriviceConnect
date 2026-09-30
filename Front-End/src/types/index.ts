// ============================================================
// ServiceConnect — Global TypeScript Types
// Based on backend entity/DTO inspection
// ============================================================

// ------ Roles ------------------------------------------------
export type Role = 'CUSTOMER' | 'PROVIDER' | 'ADMIN' | 'SUPPORT_AGENT'
export type PortalRole = 'CUSTOMER' | 'PROVIDER' | 'ADMIN' | 'SUPPORT_AGENT'

// ------ Auth -------------------------------------------------
export interface AuthUser {
  id: number
  email: string
  role: Role
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  id: number
  email: string
  role: Role
  accessToken: string
  refreshToken: string
  tokenType: 'Bearer'
  expiresIn: number
}

export interface RegisterCustomerRequest {
  email: string
  password: string
  firstName: string
  lastName: string
  phone: string
}

export interface RegisterProviderRequest {
  email: string
  password: string
  phone: string
}

export interface RegisterResponse {
  userId: number
  verificationToken: string
}

export interface RefreshTokenRequest {
  refreshToken: string
}

export interface RefreshResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export interface ForgotPasswordRequest {
  email: string
}

export interface ResetPasswordRequest {
  token: string
  newPassword: string
}

export interface VerifyEmailRequest {
  email: string
  otp: string
}

export interface RequestEmailVerificationRequest {
  email: string
}

export interface SecurityInfo {
  twoFactorEnabled: boolean
  activeSessions: number
  emailVerified: boolean
  phoneVerified: boolean
  lastLoginAt?: string
}

export interface DeleteAccountRequest {
  password: string
}

// ------ User Profile -----------------------------------------
export interface UserProfile {
  id: number
  firstName: string
  lastName: string
  phone?: string
  createdAt: string
  updatedAt: string
}

export interface UpdateProfileRequest {
  firstName: string
  lastName: string
  phone?: string
}

export interface UserSettings {
  emailNotifications?: boolean
  smsNotifications?: boolean
  [key: string]: unknown
}

export interface OnboardingStatus {
  completed: boolean
  steps?: Record<string, boolean>
}

// ------ Provider ---------------------------------------------
export type ProviderStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED'

export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY'

export interface Provider {
  id: number
  userId: number
  businessName: string
  description?: string
  phone?: string
  email?: string
  address?: string
  city?: string
  state?: string
  postalCode?: string
  latitude?: number
  longitude?: number
  status: ProviderStatus
  createdAt: string
  updatedAt: string
}

export interface ProviderPublicView {
  id: number
  businessName: string
  description?: string
  phone?: string
  email?: string
  city?: string
  state?: string
  latitude?: number
  longitude?: number
  status: ProviderStatus
}

export interface CreateProviderRequest {
  businessName: string
  description?: string
  phone: string
  email: string
  address: string
  city: string
  state: string
  postalCode: string
  latitude?: number
  longitude?: number
}

export interface UpdateProviderRequest extends Partial<CreateProviderRequest> {}

export interface UpdateLocationRequest {
  latitude: number
  longitude: number
}

export interface UpdateProviderStatusRequest {
  status: ProviderStatus
}

export interface ProviderAvailability {
  id: number
  providerId: number
  dayOfWeek: DayOfWeek
  startTime: string // HH:mm
  endTime: string // HH:mm
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateAvailabilityRequest {
  dayOfWeek: DayOfWeek
  startTime: string
  endTime: string
  active?: boolean
}

export interface UpdateAvailabilityRequest {
  dayOfWeek?: DayOfWeek
  startTime?: string
  endTime?: string
  active?: boolean
}

export interface ProviderPhoto {
  id: number
  providerId: number
  imageUrl: string
  displayOrder: number
  createdAt: string
}

export interface AddPhotoRequest {
  imageUrl: string
  displayOrder: number
}

// Provider with distance (computed client-side via Haversine)
export interface ProviderWithDistance extends ProviderPublicView {
  distanceKm?: number
  averageRating?: number
  reviewCount?: number
  photos?: ProviderPhoto[]
  availability?: ProviderAvailability[]
}

// ------ Catalog ----------------------------------------------
export type PricingType = 'FIXED' | 'STARTING_FROM' | 'QUOTE_REQUIRED'

export interface CatalogItem {
  id: number
  providerId: number
  name: string
  description?: string
  category: string
  price: number
  pricingType?: PricingType
  durationMinutes?: number
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateCatalogItemRequest {
  name: string
  description?: string
  category: string
  price: number
  pricingType?: PricingType
  durationMinutes?: number
}

export interface UpdateCatalogItemRequest extends Partial<CreateCatalogItemRequest> {}

// ------ Booking ----------------------------------------------
export type BookingStatus =
  | 'PENDING'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'COMPLETED'
  | 'CANCELLED'

export interface ServiceRequest {
  id: number
  customerId: number
  catalogItemId: number
  quoteId?: number
  providerId: number
  serviceType?: string
  description?: string
  serviceAddress?: string
  latitude?: number
  longitude?: number
  requestedStartAt: string
  requestedEndAt?: string
  priceSnapshot: number
  idempotencyKey?: string
  status: BookingStatus
  createdAt: string
  updatedAt: string
}

export interface CreateBookingRequest {
  providerId: number
  catalogItemId: number
  quoteId?: number
  description?: string
  serviceAddress: string
  latitude?: number
  longitude?: number
  requestedStartAt: string
}

export interface UpdateBookingStatusRequest {
  status: 'ACCEPTED' | 'REJECTED' | 'COMPLETED'
}

// ------ Chat & Quotes ----------------------------------------
export type QuoteStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'EXPIRED'

export interface Message {
  id: number
  conversationId: number
  senderId: number
  senderRole: string
  message: string
  readStatus: boolean
  createdAt: string
}

export interface Quote {
  id: number
  conversationId: number
  customerId: number
  providerId: number
  catalogItemId?: number
  serviceName?: string
  description: string
  note?: string
  amount: number
  currency: string
  status: QuoteStatus
  createdAt: string
  updatedAt: string
  expiresAt?: string
}

export interface Conversation {
  id: number
  customerId: number
  providerId: number
  catalogItemId?: number
  status: string
  createdAt: string
  updatedAt: string
  lastMessage?: string
  lastMessageAt?: string
  otherPartyName: string
  serviceName?: string
  messages: Message[]
  quotes: Quote[]
  customerName?: string
  providerName?: string
}

export interface CreateConversationRequest {
  providerId: number
  catalogItemId?: number
}

export interface SendMessageRequest {
  message?: string
  content?: string
}

export interface CreateQuoteRequest {
  serviceId?: number
  description: string
  amount: number
  note?: string
}


// ------ Review -----------------------------------------------
export interface Review {
  id: number
  bookingId: number
  customerId: number
  providerId: number
  rating: number // 1-5
  comment?: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateReviewRequest {
  bookingId: number
  providerId: number
  rating: number
  comment?: string
}

export interface UpdateReviewRequest {
  rating?: number
  comment?: string
}

// ------ Payment ----------------------------------------------
// IMPORTANT: Payment service uses UUID for bookingId/userId.
// Auth and Booking services use Long (number).
// This mismatch must be handled carefully — see payment.ts API module.
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED'

export interface Payment {
  id: string // UUID
  bookingId: string // UUID — mismatch with booking service Long
  userId: string // UUID — mismatch with auth service Long
  amount: number
  currency: string
  status: PaymentStatus
  paymentMethod?: string
  razorpayOrderId?: string
  razorpayPaymentId?: string
  createdAt: string
  updatedAt: string
}

export interface CreatePaymentRequest {
  bookingId: string | number // accept both until mismatch is resolved
  amount: number
  currency?: string
}

export interface CreatePaymentResponse {
  razorpayOrderId: string
  amount: number
  currency: string
}

export interface VerifyPaymentRequest {
  razorpayOrderId: string
  razorpayPaymentId: string
  razorpaySignature: string
}

export interface VerifyPaymentResponse {
  success: boolean
  paymentId?: string
  message?: string
}

// ------ Support Tickets --------------------------------------
export type TicketStatus =
  | 'OPEN'
  | 'IN_PROGRESS'
  | 'PENDING_CUSTOMER'
  | 'RESOLVED'
  | 'CLOSED'

export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'

export interface SupportTicket {
  id: number
  ticketNumber: string
  customerId: number
  assignedAgentId?: number
  subject: string
  description: string
  status: TicketStatus
  priority: TicketPriority
  category?: string
  createdAt: string
  updatedAt: string
  resolvedAt?: string
}

export interface CreateTicketRequest {
  subject: string
  description: string
  priority: TicketPriority
  category?: string
  customerId?: number
}

export interface UpdateTicketRequest {
  status?: TicketStatus
  priority?: TicketPriority
  assignedAgentId?: number
  category?: string
}

export interface AssignTicketRequest {
  agentId: number
}

export interface SupportMessage {
  id: number
  ticketId: number
  senderId: number
  content?: string
  message?: string
  createdAt: string
}


// ------ Help Center ------------------------------------------
export type HelpCategory =
  | string
  | {
      name: string
      articleCount?: number
    }

export interface HelpArticle {
  id?: number
  slug: string
  title: string
  category: string
  content: string // HTML — must be sanitized with DOMPurify before rendering
  published: boolean
  displayOrder: number
  createdAt?: string
  updatedAt?: string
}

export interface CreateHelpArticleRequest {
  slug: string
  title: string
  category: string
  content: string
  published?: boolean
  displayOrder?: number
}

export interface UpdateHelpArticleRequest extends Partial<CreateHelpArticleRequest> {}

// ------ Audit Logs -------------------------------------------
export interface AuditLog {
  id: number
  actorId: number
  actorRole: Role
  action: string
  resourceType: string
  resourceId?: string
  description?: string
  ipAddress?: string
  userAgent?: string
  createdAt: string
}

// ------ Pagination -------------------------------------------
export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface PaginationParams {
  page?: number
  size?: number
  sort?: string
}

// ------ API Error --------------------------------------------
export interface ApiError {
  status: number
  message: string
  errors?: Record<string, string>
  timestamp?: string
  path?: string
}

// ------ Map --------------------------------------------------
export interface MapBounds {
  north: number
  south: number
  east: number
  west: number
}

export interface Coordinates {
  lat: number
  lng: number
}

export type MapViewMode = 'split' | 'map-only' | 'list-only'

// ------ Onboarding -------------------------------------------
export interface ProviderOnboardingStatus {
  profileCreated: boolean
  availabilitySet: boolean
  catalogItemAdded: boolean
  photoAdded: boolean
  status: ProviderStatus
}
