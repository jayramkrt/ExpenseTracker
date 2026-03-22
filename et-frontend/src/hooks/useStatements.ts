
import { useCallback, useState, useEffect } from 'react';
import { apiClient } from '../api';
import type { StatementResponse, StatementUploadResponse } from '../api/types';
import { useFetch } from './useApi';

/**
 * Hook for statement operations.
 */
export function useStatements() {
  const [statements, setStatements] = useState<StatementResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const { data, loading, error, refetch } = useFetch(
    () => apiClient.statements.getStatements(page, 10),
    [page]
  );

  useEffect(() => {
    if (data) {
      setStatements(data.content);
      setTotalPages(data.totalPages);
    }
  }, [data]);

  // Update statements when data changes
  const updateStatements = useCallback(() => {
    if (data) {
      setStatements(data.content);
      setTotalPages(data.totalPages);
    }
  }, [data]);

  // Upload statement
  const uploadStatement = useCallback(
    async (file: File, bankName?: string, accountType?: string) => {
      try {
        const response = await apiClient.statements.uploadStatement(
          file,
          bankName,
          accountType
        );
        refetch();
        return response;
      } catch (error) {
        console.error('Upload failed:', error);
        throw error;
      }
    },
    [refetch]
  );

  // Get statement status
  const getStatus = useCallback((statementId: string) => {
    return apiClient.statements.getStatementStatus(statementId);
  }, []);

  // Delete statement
  const deleteStatement = useCallback(
    async (statementId: string) => {
      try {
        await apiClient.statements.deleteStatement(statementId);
        refetch();
      } catch (error) {
        console.error('Delete failed:', error);
        throw error;
      }
    },
    [refetch]
  );

  return {
    statements,
    loading,
    error,
    page,
    setPage,
    totalPages,
    uploadStatement,
    getStatus,
    deleteStatement,
    refetch: updateStatements,
  };
}
