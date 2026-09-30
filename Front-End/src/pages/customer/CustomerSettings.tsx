import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { userApi } from '@/api/user'
import { pushNotifications } from '@/lib/pushNotifications'
import { LoadingState, ErrorState } from '@/components/shared/UxStates'

export default function CustomerSettings() {
  const qc = useQueryClient()
  const { data: settings, isLoading, error } = useQuery({
    queryKey: ['user', 'settings'],
    queryFn: () => userApi.getSettings(),
    select: (res) => res.data,
  })

  const mutation = useMutation({
    mutationFn: (data: Record<string, unknown>) => userApi.updateSettings(data),
    onSuccess: () => {
      toast.success('Settings updated')
      qc.invalidateQueries({ queryKey: ['user', 'settings'] })
    },
    onError: () => toast.error('Failed to update settings'),
  })

  if (isLoading) return <LoadingState message="Loading settings…" />
  if (error) return <ErrorState message="Failed to load settings." action={{ label: 'Retry', onClick: () => qc.invalidateQueries({ queryKey: ['user', 'settings'] }) }} />

  const emailNotif = settings?.emailNotifications ?? true
  const smsNotif = settings?.smsNotifications ?? false

  const [pushEnabled, setPushEnabled] = useState(
    typeof window !== 'undefined' && 'Notification' in window && Notification.permission === 'granted'
  )

  const handleTogglePush = async () => {
    if (!pushEnabled) {
      const perm = await pushNotifications.requestPermission()
      if (perm === 'granted') {
        setPushEnabled(true)
        toast.success('Browser push notifications enabled')
      } else {
        toast.error('Notification permission was not granted')
      }
    } else {
      setPushEnabled(false)
      toast.info('To completely disable notifications, update permissions in browser settings')
    }
  }

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold text-[#0F172A] mb-6">Settings</h1>
      <div className="sc-card p-6 space-y-6">
        <h2 className="text-base font-semibold text-[#0F172A]">Notifications</h2>
        <div className="space-y-4">
          <label className="flex items-center justify-between cursor-pointer">
            <div>
              <p className="text-sm font-medium text-[#0F172A]">Email Notifications</p>
              <p className="text-xs text-[#64748B]">Receive booking updates and promotions via email</p>
            </div>
            <button
              type="button"
              role="switch"
              aria-checked={emailNotif}
              onClick={() => mutation.mutate({ ...settings, emailNotifications: !emailNotif })}
              className={`relative w-11 h-6 rounded-full transition-colors ${emailNotif ? 'bg-[#2563EB]' : 'bg-[#CBD5E1]'}`}
            >
              <span className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full transition-transform shadow ${emailNotif ? 'translate-x-5' : ''}`} />
            </button>
          </label>
          <label className="flex items-center justify-between cursor-pointer">
            <div>
              <p className="text-sm font-medium text-[#0F172A]">SMS Notifications</p>
              <p className="text-xs text-[#64748B]">Receive booking reminders via SMS</p>
            </div>
            <button
              type="button"
              role="switch"
              aria-checked={smsNotif}
              onClick={() => mutation.mutate({ ...settings, smsNotifications: !smsNotif })}
              className={`relative w-11 h-6 rounded-full transition-colors ${smsNotif ? 'bg-[#2563EB]' : 'bg-[#CBD5E1]'}`}
            >
              <span className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full transition-transform shadow ${smsNotif ? 'translate-x-5' : ''}`} />
            </button>
          </label>
          <label className="flex items-center justify-between cursor-pointer pt-2 border-t border-slate-100">
            <div>
              <p className="text-sm font-medium text-[#0F172A]">Browser & Web Push Notifications</p>
              <p className="text-xs text-[#64748B]">Receive instant push alerts outside the browser tab for chat and booking updates</p>
            </div>
            <button
              type="button"
              role="switch"
              aria-checked={pushEnabled}
              onClick={handleTogglePush}
              className={`relative w-11 h-6 rounded-full transition-colors ${pushEnabled ? 'bg-[#2563EB]' : 'bg-[#CBD5E1]'}`}
            >
              <span className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full transition-transform shadow ${pushEnabled ? 'translate-x-5' : ''}`} />
            </button>
          </label>
        </div>
      </div>
    </div>
  )
}
