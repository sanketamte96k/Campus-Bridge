import type { ButtonHTMLAttributes, ReactNode } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode;
  variant?: 'primary' | 'secondary';
  isLoading?: boolean;
}

export function Button({
  children,
  variant = 'primary',
  isLoading = false,
  disabled,
  type = 'button',
  className = '',
  ...rest
}: ButtonProps) {
  const baseStyles =
    'inline-flex items-center justify-center rounded-md px-4 py-2 text-sm font-medium transition-colors focus:outline-none focus:ring-2 disabled:cursor-not-allowed disabled:opacity-50';

  const variantStyles =
    variant === 'primary'
      ? 'bg-brand text-white hover:bg-brand-dark focus:ring-brand/40'
      : 'border border-line bg-white text-ink hover:bg-paper focus:ring-ink/20';

  return (
    <button
      type={type}
      disabled={disabled || isLoading}
      className={`${baseStyles} ${variantStyles} ${className}`}
      {...rest}
    >
      {isLoading ? 'Please wait...' : children}
    </button>
  );
}
