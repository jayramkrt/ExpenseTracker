
import { useCallback, useState, useEffect } from 'react';
import { apiClient } from '../api';
import type { TransactionResponse, TransactionUpdateRequest } from '../api/types';
import { useFetch } from './useApi';

/**
 * Hook for transaction operations.
 */
export function useTransactions(
  categoryId?: string,
  startDate?: string,
  endDate?: string
) {
  const [transactions, setTransactions] = useState<TransactionResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const { data, loading, error, refetch } = useFetch(
    () =>
      apiClient.transactions.getTransactions(
        page,
        20,
        categoryId,
        startDate,
        endDate
      ),
    [page, categoryId, startDate, endDate]
  );


  useEffect(() => {
    if (data) {
      setTransactions(data.content);
      setTotalPages(data.totalPages);
    }
  }, [data]);

  const updateTransactions = useCallback(() => {
    if (data) {
      setTransactions(data.content);
      setTotalPages(data.totalPages);
    }
  }, [data]);

  const updateTransaction = useCallback(
    async (transactionId: string, request: TransactionUpdateRequest) => {
      try {
        const updated = await apiClient.transactions.updateTransaction(
          transactionId,
          request
        );
        refetch();
        return updated;
      } catch (error) {
        console.error('Update failed:', error);
        throw error;
      }
    },
    [refetch]
  );

  const deleteTransaction = useCallback(
    async (transactionId: string) => {
      try {
        await apiClient.transactions.deleteTransaction(transactionId);
        refetch();
      } catch (error) {
        console.error('Delete failed:', error);
        throw error;
      }
    },
    [refetch]
  );

  const searchTransactions = useCallback(
    async (query: string) => {
      try {
        const results = await apiClient.transactions.searchTransactions(
          query,
          0,
          50
        );
        setTransactions(results.content);
        setTotalPages(results.totalPages);
      } catch (error) {
        console.error('Search failed:', error);
        throw error;
      }
    },
    []
  );

  return {
    transactions,
    loading,
    error,
    page,
    setPage,
    totalPages,
    updateTransaction,
    deleteTransaction,
    searchTransactions,
    refetch: updateTransactions,
  };
}
