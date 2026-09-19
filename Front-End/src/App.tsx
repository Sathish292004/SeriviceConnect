import { lazy } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { ProtectedRoute, LazyPage } from '@/components/auth/ProtectedRoute'
import { OfflineBanner } from '@/components/auth/ProtectedRoute'
import { useAuthStore, selectIsAuthenticated, selectRole } from '@/store/authStore'

// ---- Layouts ----
import PublicLayout from '@/layouts/PublicLayout'
import AuthLayout from '@/layouts/AuthLayout'
import CustomerLayout from '@/layouts/CustomerLayout'
import ProviderLayout from '@/layouts/ProviderLayout'
import AdminLayout from '@/layouts/AdminLayout'

// ---- Public Pages ----
const Landing = lazy(() => import('@/pages/public/Landing'))
const Services = lazy(() => import('@/pages/public/Services'))
const ProviderDiscovery = lazy(() => import('@/pages/public/ProviderDiscovery'))
const ProviderPublicProfile = lazy(() => import('@/pages/public/ProviderPublicProfile'))
const HelpCenter = lazy(() => import('@/pages/public/HelpCenter'))
const HelpCategory = lazy(() => import('@/pages/public/HelpCategory'))
const HelpArticle = lazy(() => import('@/pages/public/HelpArticle'))
const LegalPage = lazy(() => import('@/pages/public/LegalPage'))
const CookiePreferences = lazy(() => import('@/pages/public/CookiePreferences'))

// ---- Error Pages ----
const NotFound = lazy(() => import('@/pages/public/NotFound'))
const Forbidden = lazy(() => import('@/pages/public/Forbidden'))
const ServerError = lazy(() => import('@/pages/public/ServerError'))
const Maintenance = lazy(() => import('@/pages/public/Maintenance'))
const SessionExpired = lazy(() => import('@/pages/public/SessionExpired'))

// ---- Customer Auth ----
const CustomerLogin = lazy(() => import('@/pages/customer/CustomerLogin'))
const CustomerRegister = lazy(() => import('@/pages/customer/CustomerRegister'))
const CustomerVerifyEmail = lazy(() => import('@/pages/customer/CustomerVerifyEmail'))
const CustomerForgotPassword = lazy(() => import('@/pages/customer/CustomerForgotPassword'))
const CustomerResetPassword = lazy(() => import('@/pages/customer/CustomerResetPassword'))

// ---- Customer Portal ----
const CustomerDashboard = lazy(() => import('@/pages/customer/CustomerDashboard'))
const CustomerProfile = lazy(() => import('@/pages/customer/CustomerProfile'))
const CustomerSettings = lazy(() => import('@/pages/customer/CustomerSettings'))
const CustomerSecurity = lazy(() => import('@/pages/customer/CustomerSecurity'))
const CustomerServices = lazy(() => import('@/pages/customer/CustomerServices'))
const CustomerProviders = lazy(() => import('@/pages/customer/CustomerProviders'))
const CustomerProviderDetail = lazy(() => import('@/pages/customer/CustomerProviderDetail'))
const CustomerBookings = lazy(() => import('@/pages/customer/CustomerBookings'))
const CustomerBookingDetail = lazy(() => import('@/pages/customer/CustomerBookingDetail'))
const CustomerPayment = lazy(() => import('@/pages/customer/CustomerPayment'))
const PaymentSuccess = lazy(() => import('@/pages/customer/PaymentSuccess'))
const PaymentFailed = lazy(() => import('@/pages/customer/PaymentFailed'))
const PaymentPending = lazy(() => import('@/pages/customer/PaymentPending'))
const CustomerReviews = lazy(() => import('@/pages/customer/CustomerReviews'))
const CustomerSupport = lazy(() => import('@/pages/customer/CustomerSupport'))
const CustomerTickets = lazy(() => import('@/pages/customer/CustomerTickets'))
const CustomerTicketDetail = lazy(() => import('@/pages/customer/CustomerTicketDetail'))

// ---- Provider Auth ----
const ProviderLogin = lazy(() => import('@/pages/provider/ProviderLogin'))
const ProviderRegister = lazy(() => import('@/pages/provider/ProviderRegister'))
const ProviderVerifyEmail = lazy(() => import('@/pages/provider/ProviderVerifyEmail'))
const ProviderForgotPassword = lazy(() => import('@/pages/provider/ProviderForgotPassword'))
const ProviderResetPassword = lazy(() => import('@/pages/provider/ProviderResetPassword'))

// ---- Provider Portal ----
const ProviderOnboarding = lazy(() => import('@/pages/provider/ProviderOnboarding'))
const ProviderDashboard = lazy(() => import('@/pages/provider/ProviderDashboard'))
const ProviderProfile = lazy(() => import('@/pages/provider/ProviderProfile'))
const ProviderCatalog = lazy(() => import('@/pages/provider/ProviderCatalog'))
const ProviderAvailability = lazy(() => import('@/pages/provider/ProviderAvailability'))
const ProviderBookings = lazy(() => import('@/pages/provider/ProviderBookings'))
const ProviderBookingDetail = lazy(() => import('@/pages/provider/ProviderBookingDetail'))
const ProviderPhotos = lazy(() => import('@/pages/provider/ProviderPhotos'))
const ProviderSettings = lazy(() => import('@/pages/provider/ProviderSettings'))

// ---- Admin Auth ----
const AdminLogin = lazy(() => import('@/pages/admin/AdminLogin'))
const AdminForgotPassword = lazy(() => import('@/pages/admin/AdminForgotPassword'))
const AdminResetPassword = lazy(() => import('@/pages/admin/AdminResetPassword'))

// ---- Admin Portal ----
const AdminDashboard = lazy(() => import('@/pages/admin/AdminDashboard'))
const AdminCustomers = lazy(() => import('@/pages/admin/AdminCustomers'))
const AdminProviders = lazy(() => import('@/pages/admin/AdminProviders'))
const AdminProviderDetail = lazy(() => import('@/pages/admin/AdminProviderDetail'))
const AdminTickets = lazy(() => import('@/pages/admin/AdminTickets'))
const AdminTicketDetail = lazy(() => import('@/pages/admin/AdminTicketDetail'))
const AdminHelpCenter = lazy(() => import('@/pages/admin/AdminHelpCenter'))
const AdminHelpArticleForm = lazy(() => import('@/pages/admin/AdminHelpArticleForm'))
const AdminAuditLogs = lazy(() => import('@/pages/admin/AdminAuditLogs'))

// ---- Support Agent ----
const SupportTickets = lazy(() => import('@/pages/support/SupportTickets'))
const SupportTicketDetail = lazy(() => import('@/pages/support/SupportTicketDetail'))

// All legal page slugs
const LEGAL_SLUGS = [
  'privacy', 'terms', 'cookies', 'refunds', 'cancellation', 'shipping',
  'returns', 'disclaimer', 'accessibility', 'dpa', 'acceptable-use',
  'security', 'responsible-disclosure', 'community-guidelines',
]

function DashboardRedirect() {
  const role = useAuthStore(selectRole)
  const isAuthenticated = useAuthStore(selectIsAuthenticated)

  if (!isAuthenticated) {
    return <Navigate to="/customer/login" replace />
  }

  switch (role) {
    case 'PROVIDER':
      return <Navigate to="/provider/dashboard" replace />
    case 'ADMIN':
      return <Navigate to="/admin/dashboard" replace />
    case 'SUPPORT_AGENT':
      return <Navigate to="/support/tickets" replace />
    case 'CUSTOMER':
    default:
      return <Navigate to="/customer/dashboard" replace />
  }
}

export default function App() {
  return (
    <>
      <OfflineBanner />
      <Routes>
        {/* Universal Dashboard Redirect */}
        <Route path="dashboard" element={<DashboardRedirect />} />

        {/* ============================================ */}
        {/* PUBLIC ROUTES — PublicLayout                 */}
        {/* ============================================ */}
        <Route element={<PublicLayout />}>
          <Route index element={<LazyPage><Landing /></LazyPage>} />
          <Route path="services" element={<LazyPage><Services /></LazyPage>} />
          <Route path="providers" element={<LazyPage><ProviderDiscovery /></LazyPage>} />
          <Route path="providers/:id" element={<LazyPage><ProviderPublicProfile /></LazyPage>} />
          <Route path="help" element={<LazyPage><HelpCenter /></LazyPage>} />
          <Route path="help/category/:category" element={<LazyPage><HelpCategory /></LazyPage>} />
          <Route path="help/article/:slug" element={<LazyPage><HelpArticle /></LazyPage>} />

          {/* Legal pages */}
          {LEGAL_SLUGS.map((slug) => (
            <Route key={slug} path={slug} element={<LazyPage><LegalPage /></LazyPage>} />
          ))}
          <Route path="cookie-preferences" element={<LazyPage><CookiePreferences /></LazyPage>} />

          {/* Error pages */}
          <Route path="404" element={<LazyPage><NotFound /></LazyPage>} />
          <Route path="403" element={<LazyPage><Forbidden /></LazyPage>} />
          <Route path="500" element={<LazyPage><ServerError /></LazyPage>} />
          <Route path="maintenance" element={<LazyPage><Maintenance /></LazyPage>} />
          <Route path="session-expired" element={<LazyPage><SessionExpired /></LazyPage>} />
        </Route>

        {/* ============================================ */}
        {/* CUSTOMER AUTH — AuthLayout                   */}
        {/* ============================================ */}
        <Route element={<AuthLayout />}>
          <Route path="customer/login" element={<LazyPage><CustomerLogin /></LazyPage>} />
          <Route path="customer/register" element={<LazyPage><CustomerRegister /></LazyPage>} />
          <Route path="customer/verify-email" element={<LazyPage><CustomerVerifyEmail /></LazyPage>} />
          <Route path="customer/forgot-password" element={<LazyPage><CustomerForgotPassword /></LazyPage>} />
          <Route path="customer/reset-password" element={<LazyPage><CustomerResetPassword /></LazyPage>} />

          {/* PROVIDER AUTH */}
          <Route path="provider/login" element={<LazyPage><ProviderLogin /></LazyPage>} />
          <Route path="provider/register" element={<LazyPage><ProviderRegister /></LazyPage>} />
          <Route path="provider/verify-email" element={<LazyPage><ProviderVerifyEmail /></LazyPage>} />
          <Route path="provider/forgot-password" element={<LazyPage><ProviderForgotPassword /></LazyPage>} />
          <Route path="provider/reset-password" element={<LazyPage><ProviderResetPassword /></LazyPage>} />

          {/* ADMIN AUTH — no register */}
          <Route path="admin/login" element={<LazyPage><AdminLogin /></LazyPage>} />
          <Route path="admin/forgot-password" element={<LazyPage><AdminForgotPassword /></LazyPage>} />
          <Route path="admin/reset-password" element={<LazyPage><AdminResetPassword /></LazyPage>} />
        </Route>

        {/* ============================================ */}
        {/* CUSTOMER PORTAL — CustomerLayout + CUSTOMER  */}
        {/* ============================================ */}
        <Route element={<ProtectedRoute requiredRole="CUSTOMER"><CustomerLayout /></ProtectedRoute>}>
          <Route path="customer/dashboard" element={<LazyPage><CustomerDashboard /></LazyPage>} />
          <Route path="customer/profile" element={<LazyPage><CustomerProfile /></LazyPage>} />
          <Route path="customer/settings" element={<LazyPage><CustomerSettings /></LazyPage>} />
          <Route path="customer/security" element={<LazyPage><CustomerSecurity /></LazyPage>} />
          <Route path="customer/services" element={<LazyPage><CustomerServices /></LazyPage>} />
          <Route path="customer/providers" element={<LazyPage><CustomerProviders /></LazyPage>} />
          <Route path="customer/providers/:id" element={<LazyPage><CustomerProviderDetail /></LazyPage>} />
          <Route path="customer/bookings" element={<LazyPage><CustomerBookings /></LazyPage>} />
          <Route path="customer/bookings/:id" element={<LazyPage><CustomerBookingDetail /></LazyPage>} />
          <Route path="customer/payment/:bookingId" element={<LazyPage><CustomerPayment /></LazyPage>} />
          <Route path="customer/payment/success" element={<LazyPage><PaymentSuccess /></LazyPage>} />
          <Route path="customer/payment/failed" element={<LazyPage><PaymentFailed /></LazyPage>} />
          <Route path="customer/payment/pending" element={<LazyPage><PaymentPending /></LazyPage>} />
          <Route path="customer/reviews" element={<LazyPage><CustomerReviews /></LazyPage>} />
          <Route path="customer/support" element={<LazyPage><CustomerSupport /></LazyPage>} />
          <Route path="customer/support/tickets" element={<LazyPage><CustomerTickets /></LazyPage>} />
          <Route path="customer/support/tickets/:id" element={<LazyPage><CustomerTicketDetail /></LazyPage>} />
        </Route>

        {/* ============================================ */}
        {/* PROVIDER PORTAL — ProviderLayout + PROVIDER  */}
        {/* ============================================ */}
        <Route element={<ProtectedRoute requiredRole="PROVIDER"><ProviderLayout /></ProtectedRoute>}>
          <Route path="provider/onboarding" element={<LazyPage><ProviderOnboarding /></LazyPage>} />
          <Route path="provider/dashboard" element={<LazyPage><ProviderDashboard /></LazyPage>} />
          <Route path="provider/profile" element={<LazyPage><ProviderProfile /></LazyPage>} />
          <Route path="provider/catalog" element={<LazyPage><ProviderCatalog /></LazyPage>} />
          <Route path="provider/availability" element={<LazyPage><ProviderAvailability /></LazyPage>} />
          <Route path="provider/bookings" element={<LazyPage><ProviderBookings /></LazyPage>} />
          <Route path="provider/bookings/:id" element={<LazyPage><ProviderBookingDetail /></LazyPage>} />
          <Route path="provider/photos" element={<LazyPage><ProviderPhotos /></LazyPage>} />
          <Route path="provider/settings" element={<LazyPage><ProviderSettings /></LazyPage>} />
        </Route>

        {/* ============================================ */}
        {/* ADMIN PORTAL — AdminLayout + ADMIN           */}
        {/* ============================================ */}
        <Route element={<ProtectedRoute requiredRole="ADMIN"><AdminLayout /></ProtectedRoute>}>
          <Route path="admin/dashboard" element={<LazyPage><AdminDashboard /></LazyPage>} />
          <Route path="admin/customers" element={<LazyPage><AdminCustomers /></LazyPage>} />
          <Route path="admin/providers" element={<LazyPage><AdminProviders /></LazyPage>} />
          <Route path="admin/providers/:id" element={<LazyPage><AdminProviderDetail /></LazyPage>} />
          <Route path="admin/tickets" element={<LazyPage><AdminTickets /></LazyPage>} />
          <Route path="admin/tickets/:id" element={<LazyPage><AdminTicketDetail /></LazyPage>} />
          <Route path="admin/help-center" element={<LazyPage><AdminHelpCenter /></LazyPage>} />
          <Route path="admin/help-center/new" element={<LazyPage><AdminHelpArticleForm /></LazyPage>} />
          <Route path="admin/help-center/:id" element={<LazyPage><AdminHelpArticleForm /></LazyPage>} />
          <Route path="admin/audit-logs" element={<LazyPage><AdminAuditLogs /></LazyPage>} />
        </Route>

        {/* ============================================ */}
        {/* SUPPORT — AdminLayout + SUPPORT_AGENT        */}
        {/* ============================================ */}
        <Route element={<ProtectedRoute requiredRole="SUPPORT_AGENT"><AdminLayout /></ProtectedRoute>}>
          <Route path="support/tickets" element={<LazyPage><SupportTickets /></LazyPage>} />
          <Route path="support/tickets/:id" element={<LazyPage><SupportTicketDetail /></LazyPage>} />
        </Route>

        {/* ============================================ */}
        {/* CATCH-ALL → 404                             */}
        {/* ============================================ */}
        <Route path="*" element={<Navigate to="/404" replace />} />
      </Routes>
    </>
  )
}
