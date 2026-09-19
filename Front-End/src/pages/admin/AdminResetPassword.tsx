import { useNavigate, useSearchParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2, KeyRound } from 'lucide-react'
import { toast } from 'sonner'

import { authApi } from '@/api/auth'
import {
  resetPasswordSchema,
  type ResetPasswordFormValues,
} from '@/utils/validators'
import { PasswordInput } from '@/components/shared/PasswordInput'

export default function AdminResetPassword() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ResetPasswordFormValues>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: { newPassword: '', confirmPassword: '' },
  })

  const onSubmit = async (values: ResetPasswordFormValues) => {
    if (!token) {
      toast.error('Reset token is missing. Please request a new reset link.')
      return
    }

    try {
      await authApi.resetPassword({ token, newPassword: values.newPassword })
      toast.success('Password reset successfully! Please sign in.')
      navigate('/admin/login', { replace: true })
    } catch {
      toast.error(
        'Could not reset password. The link may have expired. Please request a new one.',
      )
    }
  }

  return (
    <div className="sc-card p-8">
      <div className="flex justify-center mb-4">
        <div className="w-12 h-12 rounded-full bg-[#EFF6FF] flex items-center justify-center">
          <KeyRound className="h-6 w-6 text-[#2563EB]" aria-hidden="true" />
        </div>
      </div>

      <h1 className="text-2xl font-bold text-[#0F172A] text-center mb-2">
        Reset Password
      </h1>
      <p className="text-sm text-[#64748B] text-center mb-6">
        Enter your new password below.
      </p>

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
        <div>
          <label htmlFor="newPassword" className="form-label">
            New Password
          </label>
          <PasswordInput
            id="newPassword"
            autoComplete="new-password"
            placeholder="••••••••"
            aria-invalid={!!errors.newPassword}
            aria-describedby={
              errors.newPassword ? 'newPassword-error' : undefined
            }
            {...register('newPassword')}
          />
          {errors.newPassword && (
            <p id="newPassword-error" className="form-error">
              {errors.newPassword.message}
            </p>
          )}
        </div>

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

        <button
          type="submit"
          disabled={isSubmitting}
          className="sc-btn-primary w-full"
          aria-label="Reset your password"
        >
          {isSubmitting && (
            <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />
          )}
          {isSubmitting ? 'Resetting…' : 'Reset Password'}
        </button>
      </form>
    </div>
  )
}
