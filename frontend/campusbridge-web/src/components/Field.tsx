import type { InputHTMLAttributes } from 'react';
import type { UseFormRegisterReturn } from 'react-hook-form';

interface FieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  registration?: UseFormRegisterReturn;
}

export function Field({
  label,
  error,
  registration,
  id,
  type = 'text',
  className = '',
  ...rest
}: FieldProps) {
  const inputId = id ?? registration?.name ?? label.toLowerCase().replace(/\s+/g, '-');
  const hasError = Boolean(error);

  return (
    <div className={`flex flex-col gap-1.5 ${className}`}>
      <label htmlFor={inputId} className="text-sm font-medium text-ink">
        {label}
      </label>
      <input
        id={inputId}
        type={type}
        aria-invalid={hasError}
        className={`w-full rounded-md border bg-white px-3 py-2 text-sm text-ink placeholder:text-gray-400 focus:outline-none focus:ring-2 transition-colors ${
          hasError
            ? 'border-alert focus:border-alert focus:ring-alert/20'
            : 'border-line focus:border-brand focus:ring-brand/20'
        }`}
        {...registration}
        {...rest}
      />
      {error && (
        <span role="alert" className="text-xs text-alert">
          {error}
        </span>
      )}
    </div>
  );
}
