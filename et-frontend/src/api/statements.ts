import { ApiClient } from './client';
import type {
  StatementResponse,
  StatementUploadResponse,
  StatementStatusResponse,
  PaginatedResponse,
} from './types';

/**
 * Statement API endpoints.
 */
export class StatementApi {
  constructor(private apiClient: ApiClient) {}

  /**
   * Upload a bank statement PDF.
   */
  async uploadStatement(
    file: File,
    bankName?: string,
    accountType?: string
  ): Promise<StatementUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    if (bankName) formData.append('bankName', bankName);
    if (accountType) formData.append('accountType', accountType);

    const response = await this.apiClient.getAxios().post<StatementUploadResponse>(
      '/statements/upload',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );

    return response.data;
  }

  /**
   * Get user statements with pagination.
   */
  async getStatements(
    page: number = 0,
    size: number = 10
  ): Promise<PaginatedResponse<StatementResponse>> {
    const response = await this.apiClient.getAxios().get<PaginatedResponse<StatementResponse>>(
      '/statements',
      {
        params: { page, size },
      }
    );

    return response.data;
  }

  /**
   * Get a specific statement.
   */
  async getStatement(statementId: string): Promise<StatementResponse> {
    const response = await this.apiClient.getAxios().get<StatementResponse>(
      `/statements/${statementId}`
    );

    return response.data;
  }

  /**
   * Get statement processing status.
   */
  async getStatementStatus(statementId: string): Promise<StatementStatusResponse> {
    const response = await this.apiClient.getAxios().get<StatementStatusResponse>(
      `/statements/${statementId}/status`
    );

    return response.data;
  }

  /**
   * Delete a statement.
   */
  async deleteStatement(statementId: string): Promise<void> {
    await this.apiClient.getAxios().delete(`/statements/${statementId}`);
  }

  /**
   * Retry processing a failed statement.
   */
  async retryProcessing(statementId: string): Promise<StatementResponse> {
    const response = await this.apiClient.getAxios().post<StatementResponse>(
      `/statements/${statementId}/retry`
    );

    return response.data;
  }
}