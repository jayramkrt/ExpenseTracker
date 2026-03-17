
import axios, { type AxiosInstance } from 'axios';

/**
 * Configured Axios instance for API communication.
 * Handles base URL, headers, and error handling.
 */
export class ApiClient {
  private axiosInstance: AxiosInstance;
  private baseURL: string;
  private userId: string | null = null;

  constructor(baseURL: string = 'http://localhost:8080/api/v1') {
    this.baseURL = baseURL;

    this.axiosInstance = axios.create({
      baseURL,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Add interceptor for authorization header
    this.axiosInstance.interceptors.request.use((config) => {
      if (this.userId) {
        config.headers.Authorization = this.userId;
      }
      return config;
    });

    // Add error handling interceptor
    this.axiosInstance.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          // Handle unauthorized
          this.userId = null;
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  /**
   * Set user ID for all subsequent requests.
   */
  setUserId(userId: string): void {
    this.userId = userId;
  }

  /**
   * Clear user ID (logout).
   */
  clearUserId(): void {
    this.userId = null;
  }

  /**
   * Get the underlying Axios instance for custom requests.
   */
  getAxios(): AxiosInstance {
    return this.axiosInstance;
  }
}