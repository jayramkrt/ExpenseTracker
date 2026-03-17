import { ApiClient } from './client';
import type {
  TransactionResponse,
  TransactionUpdateRequest,
  PaginatedResponse,
} from './types';

/**
 * Transaction API endpoints.
 */
export class TransactionApi {
  constructor(private apiClient: ApiClient) {}

  /**
   * Get transactions with advanced filtering.
   */
  async getTransactions(
    page: number = 0,
    size: number = 20,
    categoryId?: string,
    startDate?: string,
    endDate?: string,
    merchantName?: string,
    isManual?: boolean
  ): Promise<PaginatedResponse<TransactionResponse>> {
    const params: Record<string, any> = { page, size };

    if (categoryId) params.categoryId = categoryId;
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    if (merchantName) params.merchantName = merchantName;
    if (isManual !== undefined) params.isManual = isManual;

    const response = await this.apiClient.getAxios().get<PaginatedResponse<TransactionResponse>>(
      '/transactions',
      { params }
    );

    return response.data;
  }

  /**
   * Get a specific transaction.
   */
  async getTransaction(transactionId: string): Promise<TransactionResponse> {
    const response = await this.apiClient.getAxios().get<TransactionResponse>(
      `/transactions/${transactionId}`
    );

    return response.data;
  }

  /**
   * Update a transaction.
   */
  async updateTransaction(
    transactionId: string,
    request: TransactionUpdateRequest
  ): Promise<TransactionResponse> {
    const response = await this.apiClient.getAxios().put<TransactionResponse>(
      `/transactions/${transactionId}`,
      request
    );

    return response.data;
  }

  /**
   * Get transactions by category.
   */
  async getTransactionsByCategory(
    categoryId: string,
    startDate?: string,
    endDate?: string
  ): Promise<TransactionResponse[]> {
    const params: Record<string, any> = {};

    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;

    const response = await this.apiClient.getAxios().get<TransactionResponse[]>(
      `/transactions/category/${categoryId}`,
      { params }
    );

    return response.data;
  }

  /**
   * Search transactions.
   */
  async searchTransactions(
    query: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<TransactionResponse>> {
    const response = await this.apiClient.getAxios().get<PaginatedResponse<TransactionResponse>>(
      '/transactions/search',
      {
        params: { query, page, size },
      }
    );

    return response.data;
  }

  /**
   * Delete a transaction.
   */
  async deleteTransaction(transactionId: string): Promise<void> {
    await this.apiClient.getAxios().delete(`/transactions/${transactionId}`);
  }
}