import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2, ShieldCheck } from 'lucide-react'
import { toast } from 'sonner'

import { authApi } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'
import { loginSchema, type LoginFormValues } from '@/utils/validators'
import { PasswordInput } from '@/components/shared/PasswordInput'
import type { Role } from '@/types'

const STAFF_ROLES: Role[] = ['ADMIN', 'SUPPORT_AGENT']

export default function AdminLogin() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((s) => s.setAuth)

  const [roleMismatch, setRoleMismatch] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  })

  const onSubmit = async (values: LoginFormValues) => {
    setRoleMismatch(false)

    try {
      const { data } = await authApi.login(values)

      if (!STAFF_ROLES.includes(data.role)) {
        setRoleMismatch(true)
        return
      }

      setAuth({
        user: { id: data.id, email: data.email, role: data.role },
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        expiresIn: data.expiresIn,
        portalRole: data.role,
      })

      if (data.role === 'ADMIN') {
        toast.success('Welcome back, Admin.')
        navigate('/admin/dashboard', { replace: true })
      } else {
        toast.success('Welcome back, Support Agent.')
        navigate('/support/tickets', { replace: true })
      }
    } catch (err: unknown) {
      const axiosErr = err as { response?: { status?: number; data?: { detail?: string } } }
      const status = axiosErr?.response?.status
      const detail = axiosErr?.response?.data?.detail
      if (status === 401) {
        toast.error(detail || 'Invalid email or password.')
      } else if (status === 403) {
        toast.error(detail || 'Access denied. Please contact the system administrator.')
      } else if (detail) {
        toast.error(detail)
      } else {
        toast.error('Something went wrong. Please try again.')
      }
    }
  }

  return (
    <div className="sc-card p-8">
      <div className="flex justify-center mb-4">
        <div className="w-12 h-12 rounded-full bg-[#EFF6FF] flex items-center justify-center">
          <ShieldCheck className="h-6 w-6 text-[#2563EB]" aria-hidden="true" />
        </div>
      </div>

      <h1 className="text-2xl font-bold text-[#0F172A] text-center mb-6">
        Admin Login
      </h1>

      {roleMismatch && (
        <div
          role="alert"
          className="mb-4 rounded-[8px] border border-[#EF4444] bg-[#FEE2E2] px-4 py-3 text-sm text-[#7F1D1D]"
        >
          This account is not registered for staff access. Please use the
          correct portal.
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
        {/* Email */}
        <div>
          <label htmlFor="email" className="form-label">
            Email
          </label>
          <input
            id="email"
            type="email"
            autoComplete="email"
            placeholder="admin@serviceconnect.com"
            className="sc-input"
            aria-invalid={!!errors.email}
            aria-describedby={errors.email ? 'email-error' : undefined}
            {...register('email')}
          />
          {errors.email && (
            <p id="email-error" className="form-error">
              {errors.email.message}
            </p>
          )}
        </div>

        {/* Password */}
        <div>
          <div className="flex items-center justify-between mb-1.5">
            <label htmlFor="password" className="text-sm font-medium text-[#0F172A]">
              Password
            </label>
            <a
              href="/admin/forgot-password"
              className="text-xs text-[#2563EB] hover:text-[#1D4ED8] font-medium"
            >
              Forgot password?
            </a>
          </div>
          <PasswordInput
            id="password"
            autoComplete="current-password"
            placeholder="••••••••"
            aria-invalid={!!errors.password}
            aria-describedby={errors.password ? 'password-error' : undefined}
            {...register('password')}
          />
          {errors.password && (
            <p id="password-error" className="form-error">
              {errors.password.message}
            </p>
          )}
        </div>

        {/* Submit */}
        <button
          type="submit"
          disabled={isSubmitting}
          className="sc-btn-primary w-full"
          aria-label="Sign in to the admin portal"
        >
          {isSubmitting && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
          {isSubmitting ? 'Signing in…' : 'Sign In'}
        </button>
      </form>
    </div>
  )
}
