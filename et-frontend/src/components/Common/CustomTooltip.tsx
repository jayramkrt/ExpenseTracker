import React from 'react';

/**
 * Generic tooltip payload entry type.
 */
export interface TooltipPayloadEntry {
  name: string;
  value: number | string;
  color?: string;
  dataKey?: string;
  payload?: any;
}

/**
 * Generic tooltip component props.
 */
export interface GenericTooltipProps {
  active?: boolean;
  payload?: TooltipPayloadEntry[];
  label?: string;
  formatter?: (value: number | string) => string;
}

/**
 * Reusable custom tooltip component.
 * Use this in all your Recharts components.
 */
export function CustomTooltip({
  active,
  payload,
  formatter,
}: GenericTooltipProps) {
  if (active && payload && payload.length) {
    return (
      <div
        style={{
          backgroundColor: '#f9f9f9',
          border: '1px solid #ccc',
          borderRadius: '4px',
          padding: '8px 12px',
          boxShadow: '0 2px 8px rgba(0, 0, 0, 0.15)',
        }}
      >
        {payload.map((entry, index) => {
          const displayValue = formatter
            ? formatter(entry.value)
            : `${entry.value}`;

          return (
            <p
              key={index}
              style={{
                margin: '4px 0',
                color: entry.color || '#2c3e50',
              }}
            >
              <strong>{entry.name}:</strong> {displayValue}
            </p>
          );
        })}
      </div>
    );
  }

  return null;
}

/**
 * Predefined formatters for common use cases.
 */
export const TooltipFormatters = {
  currency: (value: number | string) => {
    const num = typeof value === 'number' ? value : parseFloat(value);
    return `$${num.toFixed(2)}`;
  },

  percentage: (value: number | string) => {
    const num = typeof value === 'number' ? value : parseFloat(value);
    return `${num.toFixed(1)}%`;
  },

  number: (value: number | string) => {
    const num = typeof value === 'number' ? value : parseFloat(value);
    return num.toLocaleString();
  },

  currencyK: (value: number | string) => {
    const num = typeof value === 'number' ? value : parseFloat(value);
    return `$${(num / 1000).toFixed(0)}k`;
  },
};
