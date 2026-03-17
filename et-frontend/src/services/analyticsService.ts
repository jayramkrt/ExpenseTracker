import { apiClient } from '../api';

/**
 * Business logic for analytics operations.
 */
export const analyticsService = {
  /**
   * Get analytics overview.
   */
  async getOverview(startDate?: string, endDate?: string) {
    return await apiClient.analytics.getOverview(startDate, endDate);
  },

  /**
   * Get category breakdown.
   */
  async getCategoryBreakdown(startDate?: string, endDate?: string) {
    return await apiClient.analytics.getCategoryBreakdown(startDate, endDate);
  },

  /**
   * Get monthly trends.
   */
  async getMonthlyTrends(months: number = 12) {
    return await apiClient.analytics.getMonthlyTrends(months);
  },

  /**
   * Get top merchants.
   */
  async getTopMerchants(limit: number = 10, startDate?: string, endDate?: string) {
    return await apiClient.analytics.getTopMerchants(limit, startDate, endDate);
  },

  /**
   * Get recurring transactions.
   */
  async getRecurringTransactions() {
    return await apiClient.analytics.getRecurringTransactions();
  },

  /**
   * Get anomalies.
   */
  async getAnomalies(threshold: number = 2.0) {
    return await apiClient.analytics.getAnomalies(threshold);
  },

  /**
   * Export all analytics data.
   */
  async exportAnalytics(startDate?: string, endDate?: string) {
    return await apiClient.analytics.exportAnalytics(startDate, endDate);
  },
};