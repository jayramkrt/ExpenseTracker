
import React from 'react';
import './AnalyticsCard.css';

interface AnalyticsCardProps {
  title: string;
  value: number;
  type: 'income' | 'expense' | 'balance' | 'transactions';
  isLoading?: boolean;
}

export function AnalyticsCard({ title, value, type, isLoading }: AnalyticsCardProps) {
  const getIcon = () => {
    switch (type) {
      case 'income':
        return '📈';
      case 'expense':
        return '📉';
      case 'balance':
        return '💳';
      case 'transactions':
        return '📊';
    }
  };

  const formatValue = (val: number) => {
    if (type === 'transactions') return val.toString();
    return `$${Math.abs(val).toFixed(2)}`;
  };

  return (
    <div className={`analytics-card ${type}`}>
      <div className="card-icon">{getIcon()}</div>
      <div className="card-content">
        <h4>{title}</h4>
        <p className={`card-value ${value < 0 && type !== 'transactions' ? 'negative' : ''}`}>
          {isLoading ? '...' : formatValue(value)}
        </p>
      </div>
    </div>
  );
}
