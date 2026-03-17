
import React from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import type { MonthlyTrendResponse } from '../../api/types';

interface ChartDataPoint {
  month: string;
  income: number;
  expenses: number;
  netBalance: number;
}

interface CustomTooltipProps {
  active?: boolean;
  payload?: Array<{
    name: string;
    value: number;
    color: string;
    dataKey: string;
  }>;
  label?: string;
}

/**
 * Custom tooltip for chart formatting.
 */
function CustomTooltip({ active, payload }: CustomTooltipProps) {
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
        {payload.map((entry, index) => (
          <p key={index} style={{ margin: '4px 0', color: entry.color }}>
            <strong>{entry.name}:</strong> ${Number(entry.value).toFixed(2)}
          </p>
        ))}
      </div>
    );
  }

  return null;
}

/**
 * Monthly spending trends chart.
 */
export function MonthlyTrendChart({
  data,
}: {
  data: MonthlyTrendResponse[];
}) {
  if (!data || data.length === 0) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center', color: '#6c757d' }}>
        No data available
      </div>
    );
  }

  // Format the data for display
  const chartData: ChartDataPoint[] = data.map((item) => ({
    month: item.month
      ? new Date(item.month + '-01').toLocaleDateString('en-US', {
          month: 'short',
          year: '2-digit',
        })
      : 'Unknown',
    income: typeof item.income === 'number' ? item.income : 0,
    expenses: typeof item.expenses === 'number' ? item.expenses : 0,
    netBalance: typeof item.netBalance === 'number' ? item.netBalance : 0,
  }));

  return (
    <ResponsiveContainer width="100%" height={300}>
      <LineChart
        data={chartData}
        margin={{ top: 5, right: 30, left: 0, bottom: 5 }}
      >
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis
          dataKey="month"
          angle={-45}
          textAnchor="end"
          height={80}
          tick={{ fontSize: 12 }}
        />
        <YAxis
          tickFormatter={(value) => `$${(value / 1000).toFixed(0)}k`}
          tick={{ fontSize: 12 }}
        />
        <Tooltip content={<CustomTooltip />} />
        <Legend />
        <Line
          type="monotone"
          dataKey="income"
          stroke="#27ae60"
          name="Income"
          strokeWidth={2}
          dot={{ r: 4 }}
          activeDot={{ r: 6 }}
        />
        <Line
          type="monotone"
          dataKey="expenses"
          stroke="#e74c3c"
          name="Expenses"
          strokeWidth={2}
          dot={{ r: 4 }}
          activeDot={{ r: 6 }}
        />
        <Line
          type="monotone"
          dataKey="netBalance"
          stroke="#3498db"
          name="Net Balance"
          strokeWidth={2}
          dot={{ r: 4 }}
          activeDot={{ r: 6 }}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}
