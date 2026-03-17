import { apiClient } from '../api';
import type { StatementResponse } from '../api/types';

/**
 * Business logic for statement operations.
 */
export const statementService = {
  /**
   * Upload and process a statement.
   */
  async uploadStatement(file: File, bankName?: string, accountType?: string) {
    return await apiClient.statements.uploadStatement(file, bankName, accountType);
  },

  /**
   * Get all statements for user.
   */
  async getStatements(page: number = 0, size: number = 10) {
    return await apiClient.statements.getStatements(page, size);
  },

  /**
   * Get single statement.
   */
  async getStatement(statementId: string) {
    return await apiClient.statements.getStatement(statementId);
  },

  /**
   * Check processing status with polling.
   */
  async checkStatusWithPolling(
    statementId: string,
    maxAttempts: number = 30,
    intervalMs: number = 2000
  ): Promise<StatementResponse> {
    for (let i = 0; i < maxAttempts; i++) {
      const status = await apiClient.statements.getStatementStatus(statementId);
      
      if (status.status === 'completed' || status.status === 'failed') {
        const statement = await apiClient.statements.getStatement(statementId);
        return statement;
      }

      // Wait before next check
      await new Promise((resolve) => setTimeout(resolve, intervalMs));
    }

    throw new Error('Statement processing timeout');
  },

  /**
   * Delete statement.
   */
  async deleteStatement(statementId: string) {
    return await apiClient.statements.deleteStatement(statementId);
  },

  /**
   * Retry processing.
   */
  async retryProcessing(statementId: string) {
    return await apiClient.statements.retryProcessing(statementId);
  },
};