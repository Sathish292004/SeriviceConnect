import { useState } from 'react'
import { Bell, Sparkles } from 'lucide-react'
import { toast } from 'sonner'
import { pushNotifications } from '@/lib/pushNotifications'

export default function ProviderSettings() {
  const [pushEnabled, setPushEnabled] = useState(
    typeof window !== 'undefined' && 'Notification' in window && Notification.permission === 'granted'
  )
  const [jobOrderAlerts, setJobOrderAlerts] = useState(true)
  const [chatAlerts, setChatAlerts] = useState(true)

  const handleTogglePush = async () => {
    if (!pushEnabled) {
      const perm = await pushNotifications.requestPermission()
      if (perm === 'granted') {
        setPushEnabled(true)
        toast.success('Browser push notifications enabled for partner account')
      } else {
        toast.error('Notification permission was not granted')
      }
    } else {
      setPushEnabled(false)
      toast.info('To completely disable notifications, update permissions in browser settings')
    }
  }

  return (
    <div className="max-w-2xl space-y-6">
      <h1 className="text-2xl font-bold text-[#0F172A]">Settings</h1>

      <div className="sc-card p-6 space-y-6">
        <div className="flex items-center gap-2 pb-3 border-b border-slate-100">
          <Bell className="w-5 h-5 text-indigo-600" />
          <h2 className="text-base font-semibold text-[#0F172A]">Notification Preferences</h2>
        </div>

        <div className="space-y-4">
          <label className="flex items-center justify-between cursor-pointer">
            <div>
              <p className="text-sm font-medium text-[#0F172A]">New Booking & Job Order Alerts</p>
              <p className="text-xs text-[#64748B]">Receive instant alerts whenever a customer books your service</p>
            </div>
            <button
              type="button"
              role="switch"
              aria-checked={jobOrderAlerts}
              onClick={() => {
                setJobOrderAlerts(!jobOrderAlerts)
                toast.success('Preferences updated')
              }}
              className={`relative w-11 h-6 rounded-full transition-colors ${jobOrderAlerts ? 'bg-[#2563EB]' : 'bg-[#CBD5E1]'}`}
            >
              <span className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full transition-transform shadow ${jobOrderAlerts ? 'translate-x-5' : ''}`} />
            </button>
          </label>

          <label className="flex items-center justify-between cursor-pointer">
            <div>
              <p className="text-sm font-medium text-[#0F172A]">Customer Chat & Quote Alerts</p>
              <p className="text-xs text-[#64748B]">Receive notifications when customers send messages or accept quotes</p>
            </div>
            <button
              type="button"
              role="switch"
              aria-checked={chatAlerts}
              onClick={() => {
                setChatAlerts(!chatAlerts)
                toast.success('Preferences updated')
              }}
              className={`relative w-11 h-6 rounded-full transition-colors ${chatAlerts ? 'bg-[#2563EB]' : 'bg-[#CBD5E1]'}`}
            >
              <span className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full transition-transform shadow ${chatAlerts ? 'translate-x-5' : ''}`} />
            </button>
          </label>

          <label className="flex items-center justify-between cursor-pointer pt-3 border-t border-slate-100">
            <div>
              <div className="flex items-center gap-1.5">
                <p className="text-sm font-medium text-[#0F172A]">Browser & Web Push Notifications</p>
                <Sparkles className="w-3.5 h-3.5 text-indigo-600" />
              </div>
              <p className="text-xs text-[#64748B]">Receive push alerts outside the website when new customer inquiries arrive</p>
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
