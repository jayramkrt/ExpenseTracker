
import { useCallback, useEffect, useState } from 'react';
import { apiClient, PFAApiClient } from '../api';

/**
 * Custom React hook for API interactions.
 * Provides loading, error, and data states.
 */
export function useApi() {
  const [userId, setUserId] = useState<string | null>(null);

  useEffect(() => {
    // Get user ID from localStorage or auth context
    const storedUserId = localStorage.getItem('userId');
    if (storedUserId) {
      setUserId(storedUserId);
      apiClient.setUserId(storedUserId);
    }
  }, []);

  const login = useCallback((newUserId: string) => {
    setUserId(newUserId);
    apiClient.setUserId(newUserId);
    localStorage.setItem('userId', newUserId);
  }, []);

  const logout = useCallback(() => {
    setUserId(null);
    apiClient.logout();
    localStorage.removeItem('userId');
  }, []);

  return { apiClient, userId, login, logout };
}

/**
 * Hook for fetching data with loading and error states.
 */
export function useFetch<T>(
  fetchFn: () => Promise<T>,
  dependencies: any[] = []
) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const fetch = async () => {
      try {
        setLoading(true);
        const result = await fetchFn();
        setData(result);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err : new Error('Unknown error'));
        setData(null);
      } finally {
        setLoading(false);
      }
    };

    fetch();
  }, dependencies);

  const refetch = useCallback(async () => {
    try {
      setLoading(true);
      const result = await fetchFn();
      setData(result);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('Unknown error'));
    } finally {
      setLoading(false);
    }
  }, [fetchFn]);

  return { data, loading, error, refetch };
}