import { apiClient } from '../api';
import type { TransactionResponse, TransactionUpdateRequest } from '../api/types';

/**
 * Business logic for transaction operations.
 */
export const transactionService = {
  /**
   * Get transactions with filtering.
   */
  async getTransactions(
    page: number = 0,
    size: number = 20,
    filters?: {
      categoryId?: string;
      startDate?: string;
      endDate?: string;
      merchantName?: string;
      isManual?: boolean;
    }
  ) {
    return await apiClient.transactions.getTransactions(
      page,
      size,
      filters?.categoryId,
      filters?.startDate,
      filters?.endDate,
      filters?.merchantName,
      filters?.isManual
    );
  },

  /**
   * Get single transaction.
   */
  async getTransaction(transactionId: string) {
    return await apiClient.transactions.getTransaction(transactionId);
  },

  /**
   * Update transaction.
   */
  async updateTransaction(transactionId: string, request: TransactionUpdateRequest) {
    return await apiClient.transactions.updateTransaction(transactionId, request);
  },

  /**
   * Search transactions.
   */
  async searchTransactions(query: string, page: number = 0) {
    return await apiClient.transactions.searchTransactions(query, page, 50);
  },

  /**
   * Delete transaction.
   */
  async deleteTransaction(transactionId: string) {
    return await apiClient.transactions.deleteTransaction(transactionId);
  },

  /**
   * Reclassify transaction.
   */
  async reclassifyTransaction(
    transactionId: string,
    categoryId: string,
    categoryName: string
  ) {
    return await apiClient.transactions.updateTransaction(transactionId, {
      categoryId,
      categoryName,
    });
  },
};
