
import { useCallback } from 'react';
import { apiClient } from '../api';
import type {
  AnalyticsResponse,
  CategoryBreakdownResponse,
  MonthlyTrendResponse,
} from '../api/types';
import { useFetch } from './useApi';

/**
 * Hook for analytics operations.
 */
export function useAnalytics(startDate?: string, endDate?: string) {
  const { data: overview, loading: overviewLoading, error: overviewError } =
    useFetch(
      () => apiClient.analytics.getOverview(startDate, endDate),
      [startDate, endDate]
    );

  const { data: categoryBreakdown, loading: categoryLoading } = useFetch(
    () => apiClient.analytics.getCategoryBreakdown(startDate, endDate),
    [startDate, endDate]
  );

  const { data: monthlyTrends, loading: trendsLoading } = useFetch(
    () => apiClient.analytics.getMonthlyTrends(12),
    []
  );

  const { data: topMerchants, loading: merchantsLoading } = useFetch(
    () => apiClient.analytics.getTopMerchants(10, startDate, endDate),
    [startDate, endDate]
  );

  const { data: recurringTransactions, loading: recurringLoading } = useFetch(
    () => apiClient.analytics.getRecurringTransactions(),
    []
  );

  const getAnomalies = useCallback(
    async (threshold: number = 2.0) => {
      return await apiClient.analytics.getAnomalies(threshold);
    },
    []
  );

  const isLoading =
    overviewLoading ||
    categoryLoading ||
    trendsLoading ||
    merchantsLoading ||
    recurringLoading;

  return {
    overview: overview as AnalyticsResponse | null,
    categoryBreakdown: categoryBreakdown as CategoryBreakdownResponse[] | null,
    monthlyTrends: monthlyTrends as MonthlyTrendResponse[] | null,
    topMerchants,
    recurringTransactions,
    getAnomalies,
    isLoading,
    error: overviewError,
  };
}