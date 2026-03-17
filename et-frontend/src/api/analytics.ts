
import { ApiClient } from './client';
import type {
  AnalyticsResponse,
  CategoryBreakdownResponse,
  MonthlyTrendResponse,
  TopMerchantResponse,
  RecurringTransactionResponse,
  AnomalyResponse,
} from './types';

/**
 * Analytics API endpoints.
 */
export class AnalyticsApi {
  constructor(private apiClient: ApiClient) {}

  /**
   * Get analytics overview.
   */
  async getOverview(
    startDate?: string,
    endDate?: string
  ): Promise<AnalyticsResponse> {
    const params: Record<string, any> = {};

    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;

    const response = await this.apiClient.getAxios().get<AnalyticsResponse>(
      '/analytics/overview',
      { params }
    );

    return response.data;
  }

  /**
   * Get category breakdown.
   */
  async getCategoryBreakdown(
    startDate?: string,
    endDate?: string
  ): Promise<CategoryBreakdownResponse[]> {
    const params: Record<string, any> = {};

    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;

    const response = await this.apiClient.getAxios().get<CategoryBreakdownResponse[]>(
      '/analytics/category-breakdown',
      { params }
    );

    return response.data;
  }

  /**
   * Get monthly trends.
   */
  async getMonthlyTrends(months: number = 12): Promise<MonthlyTrendResponse[]> {
    const response = await this.apiClient.getAxios().get<MonthlyTrendResponse[]>(
      '/analytics/monthly-trends',
      {
        params: { months },
      }
    );

    return response.data;
  }

  /**
   * Get top merchants.
   */
  async getTopMerchants(
    limit: number = 10,
    startDate?: string,
    endDate?: string
  ): Promise<TopMerchantResponse[]> {
    const params: Record<string, any> = { limit };

    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;

    const response = await this.apiClient.getAxios().get<TopMerchantResponse[]>(
      '/analytics/top-merchants',
      { params }
    );

    return response.data;
  }

  /**
   * Get recurring transactions.
   */
  async getRecurringTransactions(): Promise<RecurringTransactionResponse[]> {
    const response = await this.apiClient.getAxios().get<RecurringTransactionResponse[]>(
      '/analytics/recurring-transactions'
    );

    return response.data;
  }

  /**
   * Get anomalies.
   */
  async getAnomalies(threshold: number = 2.0): Promise<AnomalyResponse[]> {
    const response = await this.apiClient.getAxios().get<AnomalyResponse[]>(
      '/analytics/anomalies',
      {
        params: { threshold },
      }
    );

    return response.data;
  }

  /**
   * Export analytics data.
   */
  async exportAnalytics(
    startDate?: string,
    endDate?: string
  ): Promise<Record<string, any>> {
    const params: Record<string, any> = {};

    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;

    const response = await this.apiClient.getAxios().get<Record<string, any>>(
      '/analytics/export',
      { params }
    );

    return response.data;
  }
}