import type { TooltipProps } from 'recharts';

/**
 * Typed Recharts Tooltip Props.
 * Use this in your components for better type safety.
 */
export type RechartsCurrencyTooltipProps = TooltipProps<number, string>;

/**
 * Format currency value safely.
 */
export function formatCurrencyValue(value: unknown): string {
  if (typeof value === 'number') {
    return `$${value.toFixed(2)}`;
  }
  return 'N/A';
}

/**
 * Format percentage value safely.
 */
export function formatPercentageValue(value: unknown): string {
  if (typeof value === 'number') {
    return `${value.toFixed(1)}%`;
  }
  return 'N/A';
}