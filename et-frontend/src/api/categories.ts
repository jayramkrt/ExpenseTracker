
import { ApiClient } from './client';
import type { CategoryResponse } from './types';

/**
 * Category API endpoints.
 */
export class CategoryApi {
  constructor(private apiClient: ApiClient) {}

  /**
   * Get all categories.
   */
  async getAllCategories(): Promise<CategoryResponse[]> {
    const response = await this.apiClient.getAxios().get<CategoryResponse[]>(
      '/categories'
    );

    return response.data;
  }

  /**
   * Get a specific category.
   */
  async getCategory(categoryId: string): Promise<CategoryResponse> {
    const response = await this.apiClient.getAxios().get<CategoryResponse>(
      `/categories/${categoryId}`
    );

    return response.data;
  }

  /**
   * Get system categories only.
   */
  async getSystemCategories(): Promise<CategoryResponse[]> {
    const response = await this.apiClient.getAxios().get<CategoryResponse[]>(
      '/categories/system'
    );

    return response.data;
  }
}