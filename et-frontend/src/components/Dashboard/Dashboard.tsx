
import React, { useState } from 'react';
import { useAnalytics } from '../../hooks/useAnalytics';
import { AnalyticsCard } from './AnalyticsCard';
import { CategoryChart } from './CategoryChart';
import { MonthlyTrendChart } from './MonthlyTrendChart';
import { TopMerchantsChart } from './TopMerchantsChart';
import './Dashboard.css';

export function Dashboard() {
  const [startDate, setStartDate] = useState<string>();
  const [endDate, setEndDate] = useState<string>();

  const {
    overview,
    categoryBreakdown,
    monthlyTrends,
    topMerchants,
    isLoading,
    error,
  } = useAnalytics(startDate, endDate);

  if (error) {
    return (
      <div className="error-container">
        <p className="error-message">❌ Failed to load analytics: {error.message}</p>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h2>Dashboard Overview</h2>
        <div className="date-filters">
          <input
            type="date"
            value={startDate || ''}
            onChange={(e) => setStartDate(e.target.value)}
            className="date-input"
          />
          <span className="separator">to</span>
          <input
            type="date"
            value={endDate || ''}
            onChange={(e) => setEndDate(e.target.value)}
            className="date-input"
          />
        </div>
      </div>

      {isLoading ? (
        <div className="loading">Loading dashboard...</div>
      ) : (
        <>
          {/* Summary Cards */}
          <div className="summary-grid">
            <AnalyticsCard
              title="Total Income"
              value={overview?.totalIncome || 0}
              type="income"
              isLoading={isLoading}
            />
            <AnalyticsCard
              title="Total Expenses"
              value={Math.abs(overview?.totalExpenses || 0)}
              type="expense"
              isLoading={isLoading}
            />
            <AnalyticsCard
              title="Net Balance"
              value={overview?.netBalance || 0}
              type="balance"
              isLoading={isLoading}
            />
            <AnalyticsCard
              title="Transactions"
              value={overview?.transactionCount || 0}
              type="transactions"
              isLoading={isLoading}
            />
          </div>

          {/* Charts Grid */}
          <div className="charts-grid">
            <div className="chart-container full-width">
              <h3>Monthly Trends (Last 12 Months)</h3>
              <MonthlyTrendChart data={monthlyTrends || []} />
            </div>

            <div className="chart-container">
              <h3>Spending by Category</h3>
              <CategoryChart data={categoryBreakdown || []} />
            </div>

            <div className="chart-container">
              <h3>Top Merchants</h3>
              <TopMerchantsChart data={topMerchants || []} />
            </div>
          </div>
        </>
      )}
    </div>
  );
}