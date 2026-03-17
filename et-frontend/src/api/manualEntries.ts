import { ApiClient } from './client';
import type {
  ManualEntryRequest,
  ManualEntryResponse,
  PaginatedResponse,
} from './types';

/**
 * Manual Entry API endpoints.
 */
export class ManualEntryApi {
  constructor(private apiClient: ApiClient) {}

  /**
   * Create a new manual entry.
   */
  async createManualEntry(request: ManualEntryRequest): Promise<ManualEntryResponse> {
    const response = await this.apiClient.getAxios().post<ManualEntryResponse>(
      '/manual-entries',
      request
    );

    return response.data;
  }

  /**
   * Get manual entries with filtering.
   */
  async getManualEntries(
    page: number = 0,
    size: number = 20,
    categoryId?: string,
    startDate?: string,
    endDate?: string
  ): Promise<PaginatedResponse<ManualEntryResponse>> {
    const params: Record<string, any> = { page, size };

    if (categoryId) params.categoryId = categoryId;
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;

    const response = await this.apiClient.getAxios().get<PaginatedResponse<ManualEntryResponse>>(
      '/manual-entries',
      { params }
    );

    return response.data;
  }

  /**
   * Get a specific manual entry.
   */
  async getManualEntry(entryId: string): Promise<ManualEntryResponse> {
    const response = await this.apiClient.getAxios().get<ManualEntryResponse>(
      `/manual-entries/${entryId}`
    );

    return response.data;
  }

  /**
   * Update a manual entry.
   */
  async updateManualEntry(
    entryId: string,
    request: ManualEntryRequest
  ): Promise<ManualEntryResponse> {
    const response = await this.apiClient.getAxios().put<ManualEntryResponse>(
      `/manual-entries/${entryId}`,
      request
    );

    return response.data;
  }

  /**
   * Delete a manual entry.
   */
  async deleteManualEntry(entryId: string): Promise<void> {
    await this.apiClient.getAxios().delete(`/manual-entries/${entryId}`);
  }
}