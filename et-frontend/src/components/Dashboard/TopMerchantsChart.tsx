
import React from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  type TooltipProps,
} from 'recharts';

interface MerchantData {
  merchantName: string;
  totalSpending: number;
  transactionCount: number;
}

/**
 * Custom tooltip for merchant chart.
 */
function MerchantTooltip(props: TooltipProps<number, string>) {
  const { active, payload } = props;

  if (active && payload && payload.length) {
    const data = payload[0].payload as MerchantData;
    return (
      <div
        style={{
          backgroundColor: '#f9f9f9',
          border: '1px solid #ccc',
          borderRadius: '4px',
          padding: '8px 12px',
        }}
      >
        <p style={{ margin: '4px 0', fontWeight: 'bold' }}>
          {data.merchantName}
        </p>
        <p style={{ margin: '4px 0', color: '#e74c3c' }}>
          Spending: ${Number(payload[0].value).toFixed(2)}
        </p>
        <p style={{ margin: '4px 0', color: '#6c757d' }}>
          Transactions: {data.transactionCount}
        </p>
      </div>
    );
  }

  return null;
}

/**
 * Top merchants spending chart.
 */
export function TopMerchantsChart({
  data,
}: {
  data: MerchantData[];
}) {
  if (!data || data.length === 0) {
    return <div className="chart-empty">No merchant data available</div>;
  }

  return (
    <ResponsiveContainer width="100%" height={300}>
      <BarChart
        data={data}
        margin={{ top: 5, right: 30, left: 0, bottom: 5 }}
        layout="vertical"
      >
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis
          type="number"
          tickFormatter={(value) => `$${(value / 1000).toFixed(0)}k`}
          tick={{ fontSize: 12 }}
        />
        <YAxis
          dataKey="merchantName"
          type="category"
          tick={{ fontSize: 12 }}
          width={120}
        />
        <Tooltip content={<MerchantTooltip />} />
        <Bar
          dataKey="totalSpending"
          fill="#667eea"
          name="Total Spending"
          radius={[0, 8, 8, 0]}
        />
      </BarChart>
    </ResponsiveContainer>
  );
}
