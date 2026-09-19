import { useQuery, useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { Shield, Key, LogOut } from 'lucide-react'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'
import { changePasswordSchema, type ChangePasswordFormValues } from '@/utils/validators'
import { PasswordInput } from '@/components/shared/PasswordInput'
import { LoadingState, ErrorState } from '@/components/shared/UxStates'

export default function CustomerSecurity() {
  const { data: security, isLoading, error } = useQuery({
    queryKey: ['auth', 'security'],
    queryFn: () => authApi.getSecurityInfo(),
    select: (res) => res.data,
  })

  const { register, handleSubmit, formState: { errors }, reset } = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema),
  })

  const changePwMutation = useMutation({
    mutationFn: (data: ChangePasswordFormValues) =>
      authApi.changePassword({ currentPassword: data.currentPassword, newPassword: data.newPassword }),
    onSuccess: () => { toast.success('Password changed successfully'); reset() },
    onError: () => toast.error('Failed to change password. Check your current password.'),
  })

  const logoutAllMutation = useMutation({
    mutationFn: () => authApi.logoutAll(),
    onSuccess: () => { toast.success('All sessions logged out'); useAuthStore.getState().clearAuth() },
    onError: () => toast.error('Failed to logout all sessions'),
  })

  if (isLoading) return <LoadingState message="Loading security info…" />
  if (error) return <ErrorState message="Failed to load security info." />

  return (
    <div className="max-w-2xl space-y-6">
      <h1 className="text-2xl font-bold text-[#0F172A]">Security</h1>

      {/* Security overview */}
      <div className="sc-card p-6">
        <h2 className="text-base font-semibold text-[#0F172A] mb-4 flex items-center gap-2">
          <Shield className="w-5 h-5 text-[#2563EB]" /> Security Overview
        </h2>
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div><span className="text-[#64748B]">Email Verified:</span> <span className={security?.emailVerified ? 'text-[#16A34A] font-medium' : 'text-[#EF4444]'}>{security?.emailVerified ? 'Yes' : 'No'}</span></div>
          <div><span className="text-[#64748B]">Phone Verified:</span> <span className={security?.phoneVerified ? 'text-[#16A34A] font-medium' : 'text-[#EF4444]'}>{security?.phoneVerified ? 'Yes' : 'No'}</span></div>
          <div><span className="text-[#64748B]">Active Sessions:</span> <span className="font-medium">{security?.activeSessions ?? 'N/A'}</span></div>
        </div>
      </div>

      {/* Change password */}
      <form onSubmit={handleSubmit((d) => changePwMutation.mutate(d))} className="sc-card p-6 space-y-4">
        <h2 className="text-base font-semibold text-[#0F172A] flex items-center gap-2">
          <Key className="w-5 h-5 text-[#F59E0B]" /> Change Password
        </h2>
        <div>
          <label htmlFor="currentPassword" className="form-label">Current Password</label>
          <PasswordInput id="currentPassword" {...register('currentPassword')} />
          {errors.currentPassword && <p className="form-error">{errors.currentPassword.message}</p>}
        </div>
        <div>
          <label htmlFor="newPassword" className="form-label">New Password</label>
          <PasswordInput id="newPassword" {...register('newPassword')} />
          {errors.newPassword && <p className="form-error">{errors.newPassword.message}</p>}
        </div>
        <div>
          <label htmlFor="confirmPassword" className="form-label">Confirm New Password</label>
          <PasswordInput id="confirmPassword" {...register('confirmPassword')} />
          {errors.confirmPassword && <p className="form-error">{errors.confirmPassword.message}</p>}
        </div>
        <button type="submit" disabled={changePwMutation.isPending} className="sc-btn-primary text-sm">
          {changePwMutation.isPending ? 'Changing…' : 'Change Password'}
        </button>
      </form>

      {/* Logout all */}
      <div className="sc-card p-6">
        <h2 className="text-base font-semibold text-[#0F172A] flex items-center gap-2 mb-3">
          <LogOut className="w-5 h-5 text-[#EF4444]" /> Sessions
        </h2>
        <p className="text-sm text-[#64748B] mb-4">Log out of all devices and sessions. You will need to log in again.</p>
        <button onClick={() => logoutAllMutation.mutate()} disabled={logoutAllMutation.isPending} className="sc-btn-danger text-sm">
          {logoutAllMutation.isPending ? 'Logging out…' : 'Logout All Sessions'}
        </button>
      </div>
    </div>
  )
}
