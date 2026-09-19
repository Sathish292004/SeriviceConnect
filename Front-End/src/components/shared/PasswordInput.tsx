import React, { useState, forwardRef } from 'react'
import { Eye, EyeOff } from 'lucide-react'

export interface PasswordInputProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'type'> {
  error?: string
}

export const PasswordInput = forwardRef<HTMLInputElement, PasswordInputProps>(
  ({ className = '', disabled, id, ...props }, ref) => {
    const [showPassword, setShowPassword] = useState(false)

    return (
      <div className="relative w-full">
        <input
          ref={ref}
          id={id}
          type={showPassword ? 'text' : 'password'}
          disabled={disabled}
          className={`sc-input pr-11 ${className}`}
          {...props}
        />
        <button
          type="button"
          tabIndex={0}
          disabled={disabled}
          onClick={() => setShowPassword((prev) => !prev)}
          className="absolute right-0 top-0 bottom-0 px-3.5 flex items-center justify-center text-[#64748B] hover:text-[#0F172A] focus:outline-none focus-visible:ring-2 focus-visible:ring-[#2563EB] rounded-r-[8px] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          aria-label={showPassword ? 'Hide password' : 'Show password'}
          title={showPassword ? 'Hide password' : 'Show password'}
        >
          {showPassword ? (
            <EyeOff className="w-4 h-4 text-[#64748B] hover:text-[#0F172A]" aria-hidden="true" />
          ) : (
            <Eye className="w-4 h-4 text-[#64748B] hover:text-[#0F172A]" aria-hidden="true" />
          )}
        </button>
      </div>
    )
  }
)

PasswordInput.displayName = 'PasswordInput'

export default PasswordInput
