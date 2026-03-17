
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import { LoadingSpinner } from '../Common/LoadingSpinner';
import type { StatementResponse } from '../../api/types';
import './StatementsList.css';

/**
 * Display list of bank statements with actions.
 */
export function StatementsList() {
  const navigate = useNavigate();
  const { apiClient, userId } = useApi();

  const [statements, setStatements] = useState<StatementResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Fetch statements
  useEffect(() => {
    const fetchStatements = async () => {
      try {
        if (!userId) return;

        setLoading(true);
        const response = await apiClient.statements.getStatements(page, 10);
        
        // Handle response - could be paginated or direct array
        if ('content' in response) {
          setStatements(response.content);
          setTotalPages(response.totalPages);
        } else if (Array.isArray(response)) {
          setStatements(response);
          setTotalPages(1);
        }
        
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load statements');
        setStatements([]);
      } finally {
        setLoading(false);
      }
    };

    fetchStatements();
  }, [page, userId, apiClient]);

  const handleViewDetail = (statementId: string) => {
    navigate(`/statements/${statementId}`);
  };

  const handleDelete = async (statementId: string) => {
    if (!window.confirm('Are you sure you want to delete this statement?')) {
      return;
    }

    try {
      await apiClient.statements.deleteStatement(statementId);
      setStatements(statements.filter((s) => s.id !== statementId));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete statement');
    }
  };

  const handleRetry = async (statementId: string) => {
    try {
      await apiClient.statements.retryProcessing(statementId);
      // Refresh the list
      const response = await apiClient.statements.getStatements(page, 10);
      if ('content' in response) {
        setStatements(response.content);
      } else if (Array.isArray(response)) {
        setStatements(response);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to retry processing');
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'pending':
        return '#f39c12';
      case 'processing':
        return '#3498db';
      case 'completed':
        return '#27ae60';
      case 'failed':
        return '#e74c3c';
      default:
        return '#6c757d';
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading statements..." />;
  }

  if (error) {
    return (
      <div className="error-message">
        <p>{error}</p>
      </div>
    );
  }

  if (!statements || statements.length === 0) {
    return (
      <div className="statements-empty">
        <h3>No Statements Yet</h3>
        <p>Upload a bank statement PDF to get started.</p>
      </div>
    );
  }

  return (
    <div className="statements-list">
      <div className="statements-grid">
        {statements.map((statement) => (
          <div key={statement.id} className="statement-card">
            <div className="statement-header">
              <h3 className="statement-filename">{statement.filename}</h3>
              <span
                className="status-badge"
                style={{ backgroundColor: getStatusColor(statement.processingStatus) }}
              >
                {statement.processingStatus}
              </span>
            </div>

            <div className="statement-details">
              {statement.bankName && (
                <div className="detail-row">
                  <span className="label">Bank:</span>
                  <span className="value">{statement.bankName}</span>
                </div>
              )}

              {statement.accountType && (
                <div className="detail-row">
                  <span className="label">Type:</span>
                  <span className="value">{statement.accountType}</span>
                </div>
              )}

              <div className="detail-row">
                <span className="label">Transactions:</span>
                <span className="value">{statement.transactionCount || 0}</span>
              </div>

              <div className="detail-row">
                <span className="label">Uploaded:</span>
                <span className="value">
                  {new Date(statement.uploadedAt).toLocaleDateString()}
                </span>
              </div>
            </div>

            {statement.errorMessage && (
              <div className="error-info">
                <p className="error-label">Error:</p>
                <p className="error-text">{statement.errorMessage}</p>
              </div>
            )}

            <div className="statement-actions">
              <button
                className="btn btn-primary"
                onClick={() => handleViewDetail(statement.id)}
              >
                View Details
              </button>

              {statement.processingStatus === 'failed' && (
                <button
                  className="btn btn-retry"
                  onClick={() => handleRetry(statement.id)}
                >
                  Retry
                </button>
              )}

              <button
                className="btn btn-danger"
                onClick={() => handleDelete(statement.id)}
              >
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="pagination">
          <button
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
            className="pagination-btn"
          >
            Previous
          </button>

          <span className="pagination-info">
            Page {page + 1} of {totalPages}
          </span>

          <button
            disabled={page >= totalPages - 1}
            onClick={() => setPage(page + 1)}
            className="pagination-btn"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}