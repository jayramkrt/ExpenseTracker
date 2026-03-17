
import { ApiClient } from './client';
import { StatementApi } from './statements';
import { TransactionApi } from './transactions';
import { ManualEntryApi } from './manualEntries';
import { CategoryApi } from './categories';
import { AnalyticsApi } from './analytics';

/**
 * Main API client combining all endpoints.
 */
export class PFAApiClient {
  private apiClient: ApiClient;

  public statements: StatementApi;
  public transactions: TransactionApi;
  public manualEntries: ManualEntryApi;
  public categories: CategoryApi;
  public analytics: AnalyticsApi;

  constructor(baseURL?: string) {
    this.apiClient = new ApiClient(baseURL);

    this.statements = new StatementApi(this.apiClient);
    this.transactions = new TransactionApi(this.apiClient);
    this.manualEntries = new ManualEntryApi(this.apiClient);
    this.categories = new CategoryApi(this.apiClient);
    this.analytics = new AnalyticsApi(this.apiClient);
  }

  /**
   * Set user ID for authentication.
   */
  setUserId(userId: string): void {
    this.apiClient.setUserId(userId);
  }

  /**
   * Clear user ID (logout).
   */
  logout(): void {
    this.apiClient.clearUserId();
  }
}

// Export types for use in components
export * from './types';

// Export singleton instance (optional)
//export const apiClient = new PFAApiClient(
//  process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1'
//);


/**
 * Get API base URL from environment or use default.
 * 
 * For Vite: Use import.meta.env.VITE_API_URL
 * For CRA: Use import.meta.env.REACT_APP_API_URL
 */
function getApiUrl(): string {
  // Try Vite environment variable first
  if (import.meta.env.VITE_API_URL) {
    return import.meta.env.VITE_API_URL;
  }
  
  // Fallback to default
  return 'http://localhost:8080/api/v1';
}


// Export singleton instance
export const apiClient = new PFAApiClient(getApiUrl());