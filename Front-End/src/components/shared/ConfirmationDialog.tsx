import { AlertTriangle, AlertCircle, HelpCircle } from 'lucide-react'
import { Modal } from './Modal'

interface ConfirmationDialogProps {
  isOpen: boolean
  onClose: () => void
  onConfirm: () => void
  title: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
  variant?: 'danger' | 'warning' | 'primary'
  isLoading?: boolean
}

export function ConfirmationDialog({
  isOpen,
  onClose,
  onConfirm,
  title,
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  variant = 'danger',
  isLoading = false,
}: ConfirmationDialogProps) {
  const getIcon = () => {
    switch (variant) {
      case 'danger':
        return (
          <div className="w-12 h-12 rounded-full bg-rose-50 border border-rose-100 flex items-center justify-center flex-shrink-0 text-rose-600 mb-4">
            <AlertTriangle className="w-6 h-6" />
          </div>
        )
      case 'warning':
        return (
          <div className="w-12 h-12 rounded-full bg-amber-50 border border-amber-100 flex items-center justify-center flex-shrink-0 text-amber-600 mb-4">
            <AlertCircle className="w-6 h-6" />
          </div>
        )
      case 'primary':
      default:
        return (
          <div className="w-12 h-12 rounded-full bg-blue-50 border border-blue-100 flex items-center justify-center flex-shrink-0 text-blue-600 mb-4">
            <HelpCircle className="w-6 h-6" />
          </div>
        )
    }
  }

  const getConfirmButtonClass = () => {
    switch (variant) {
      case 'danger':
        return 'bg-rose-600 hover:bg-rose-700 focus-visible:ring-rose-500 text-white'
      case 'warning':
        return 'bg-amber-600 hover:bg-amber-700 focus-visible:ring-amber-500 text-white'
      case 'primary':
      default:
        return 'bg-blue-600 hover:bg-blue-700 focus-visible:ring-blue-500 text-white'
    }
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} maxWidth="sm">
      <div className="text-center sm:text-left flex flex-col sm:flex-row items-center sm:items-start gap-4">
        {getIcon()}
        <div className="flex-1">
          <h3 className="text-lg font-bold text-slate-900">{title}</h3>
          <p className="text-sm text-slate-500 mt-2 leading-relaxed">{message}</p>
        </div>
      </div>

      <div className="mt-6 flex flex-col-reverse sm:flex-row sm:justify-end gap-3">
        <button
          type="button"
          onClick={onClose}
          disabled={isLoading}
          className="w-full sm:w-auto px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors disabled:opacity-50"
        >
          {cancelLabel}
        </button>
        <button
          type="button"
          onClick={onConfirm}
          disabled={isLoading}
          className={`w-full sm:w-auto px-5 py-2.5 rounded-xl text-sm font-semibold shadow-sm transition-all disabled:opacity-50 flex items-center justify-center gap-2 ${getConfirmButtonClass()}`}
        >
          {isLoading ? (
            <>
              <div className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin" />
              <span>Processing...</span>
            </>
          ) : (
            confirmLabel
          )}
        </button>
      </div>
    </Modal>
  )
}
