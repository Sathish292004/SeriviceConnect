import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { toast } from 'sonner'

import { authApi } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'
import { loginSchema, type LoginFormValues } from '@/utils/validators'
import { PasswordInput } from '@/components/shared/PasswordInput'
import type { Role } from '@/types'

const EXPECTED_ROLE: Role = 'PROVIDER'

export default function ProviderLogin() {
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

      if (data.role !== EXPECTED_ROLE) {
        setRoleMismatch(true)
        return
      }

      setAuth({
        user: { id: data.id, email: data.email, role: data.role },
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        expiresIn: data.expiresIn,
        portalRole: EXPECTED_ROLE,
      })

      toast.success('Welcome back!')
      navigate('/provider/dashboard', { replace: true })
    } catch (err: unknown) {
      const axiosErr = err as { response?: { status?: number; data?: { detail?: string } } }
      const status = axiosErr?.response?.status
      const detail = axiosErr?.response?.data?.detail
      if (status === 401) {
        toast.error(detail || 'Invalid email or password.')
      } else if (status === 403) {
        toast.error(detail || 'Your account has been suspended. Please contact support.')
      } else if (detail) {
        toast.error(detail)
      } else {
        toast.error('Something went wrong. Please try again.')
      }
    }
  }

  return (
    <div className="sc-card p-8">
      <h1 className="text-2xl font-bold text-[#0F172A] text-center mb-6">
        Provider Login
      </h1>

      {roleMismatch && (
        <div
          role="alert"
          className="mb-4 rounded-[8px] border border-[#EF4444] bg-[#FEE2E2] px-4 py-3 text-sm text-[#7F1D1D]"
        >
          This account is not registered as a provider. Please use the correct
          portal.
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
            placeholder="you@example.com"
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
            <Link
              to="/provider/forgot-password"
              className="text-xs text-[#2563EB] hover:text-[#1D4ED8] font-medium"
            >
              Forgot password?
            </Link>
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
          aria-label="Sign in to your provider account"
        >
          {isSubmitting && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
          {isSubmitting ? 'Signing in…' : 'Sign In'}
        </button>
      </form>

      {/* Footer links */}
      <div className="mt-6 space-y-3 text-center text-sm">
        <p className="text-[#64748B]">
          Don&apos;t have an account?{' '}
          <Link
            to="/provider/register"
            className="text-[#2563EB] hover:text-[#1D4ED8] font-medium"
          >
            Register
          </Link>
        </p>
        <p className="text-[#64748B]">
          Are you a customer?{' '}
          <Link
            to="/customer/login"
            className="text-[#2563EB] hover:text-[#1D4ED8] font-medium"
          >
            Customer Login
          </Link>
        </p>
      </div>
    </div>
  )
}
