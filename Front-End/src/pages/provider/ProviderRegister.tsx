import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { toast } from 'sonner'

import { authApi } from '@/api/auth'
import {
  registerProviderSchema,
  type RegisterProviderFormValues,
} from '@/utils/validators'
import { PasswordInput } from '@/components/shared/PasswordInput'

export default function ProviderRegister() {
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterProviderFormValues>({
    resolver: zodResolver(registerProviderSchema),
    defaultValues: {
      email: '',
      phone: '',
      password: '',
      confirmPassword: '',
    },
  })

  const onSubmit = async (values: RegisterProviderFormValues) => {
    try {
      await authApi.registerProvider({
        email: values.email,
        phone: values.phone,
        password: values.password,
      })

      toast.success('Account created! Please verify your email.')
      navigate(
        `/provider/verify-email?email=${encodeURIComponent(values.email)}`,
        { replace: true },
      )
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status
      if (status === 409) {
        toast.error('Email already registered. Please log in instead.')
      } else {
        toast.error('Registration failed. Please try again.')
      }
    }
  }

  return (
    <div className="sc-card p-8">
      <h1 className="text-2xl font-bold text-[#0F172A] text-center mb-6">
        Create Provider Account
      </h1>

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

        {/* Phone */}
        <div>
          <label htmlFor="phone" className="form-label">
            Phone
          </label>
          <input
            id="phone"
            type="tel"
            autoComplete="tel"
            placeholder="+91 98765 43210"
            className="sc-input"
            aria-invalid={!!errors.phone}
            aria-describedby={errors.phone ? 'phone-error' : undefined}
            {...register('phone')}
          />
          {errors.phone && (
            <p id="phone-error" className="form-error">
              {errors.phone.message}
            </p>
          )}
        </div>

        {/* Password */}
        <div>
          <label htmlFor="password" className="form-label">
            Password
          </label>
          <PasswordInput
            id="password"
            autoComplete="new-password"
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

        {/* Confirm Password */}
        <div>
          <label htmlFor="confirmPassword" className="form-label">
            Confirm Password
          </label>
          <PasswordInput
            id="confirmPassword"
            autoComplete="new-password"
            placeholder="••••••••"
            aria-invalid={!!errors.confirmPassword}
            aria-describedby={
              errors.confirmPassword ? 'confirmPassword-error' : undefined
            }
            {...register('confirmPassword')}
          />
          {errors.confirmPassword && (
            <p id="confirmPassword-error" className="form-error">
              {errors.confirmPassword.message}
            </p>
          )}
        </div>

        {/* Submit */}
        <button
          type="submit"
          disabled={isSubmitting}
          className="sc-btn-primary w-full"
          aria-label="Create your provider account"
        >
          {isSubmitting && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
          {isSubmitting ? 'Creating account…' : 'Create Account'}
        </button>
      </form>

      {/* Footer link */}
      <p className="mt-6 text-center text-sm text-[#64748B]">
        Already have an account?{' '}
        <Link
          to="/provider/login"
          className="text-[#2563EB] hover:text-[#1D4ED8] font-medium"
        >
          Sign In
        </Link>
      </p>
    </div>
  )
}
