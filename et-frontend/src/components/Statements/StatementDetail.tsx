
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import type { StatementResponse } from '../../api/types';
import './StatementDetail.css';

/**
 * Detailed view of a single statement with transaction list.
 */
export function StatementDetail() {
  const { statementId } = useParams<{ statementId: string }>();
  const navigate = useNavigate();
  const { apiClient, userId } = useApi();
  const [statement, setStatement] = useState<StatementResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStatement = async () => {
      try {
        if (!statementId || !userId) return;
        
        const data = await apiClient.statements.getStatement(statementId);
        setStatement(data);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load statement');
      } finally {
        setLoading(false);
      }
    };

    fetchStatement();
  }, [statementId, userId, apiClient]);

  if (loading) return <div className="loading">Loading statement...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!statement) return <div className="error">Statement not found</div>;

  const statusColor = {
    pending: '#f39c12',
    processing: '#3498db',
    completed: '#27ae60',
    failed: '#e74c3c',
  };

  return (
    <div className="statement-detail">
      <button onClick={() => navigate('/statements')} className="back-button">
        ← Back to Statements
      </button>

      <div className="detail-header">
        <h2>{statement.filename}</h2>
        <div className="detail-meta">
          <div className="meta-item">
            <span className="label">Status:</span>
            <span
              className="status-badge"
              style={{ backgroundColor: statusColor[statement.processingStatus as keyof typeof statusColor] }}
            >
              {statement.processingStatus}
            </span>
          </div>
          <div className="meta-item">
            <span className="label">Uploaded:</span>
            <span>{new Date(statement.uploadedAt).toLocaleDateString()}</span>
          </div>
          <div className="meta-item">
            <span className="label">Transactions:</span>
            <span>{statement.transactionCount}</span>
          </div>
        </div>
      </div>

      {statement.bankName && (
        <div className="detail-section">
          <h3>Bank Information</h3>
          <p><strong>Bank:</strong> {statement.bankName}</p>
          {statement.accountType && <p><strong>Account Type:</strong> {statement.accountType}</p>}
          {statement.accountLastFour && <p><strong>Account:</strong> ****{statement.accountLastFour}</p>}
        </div>
      )}

      {statement.errorMessage && (
        <div className="error-section">
          <h3>Error</h3>
          <p>{statement.errorMessage}</p>
        </div>
      )}

      <div className="action-buttons">
        {statement.processingStatus === 'failed' && (
          <button className="btn btn-primary">Retry Processing</button>
        )}
        <button className="btn btn-secondary">View Transactions</button>
        <button className="btn btn-danger">Delete Statement</button>
      </div>
    </div>
  );
}