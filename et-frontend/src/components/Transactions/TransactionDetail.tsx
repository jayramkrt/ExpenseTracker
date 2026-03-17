import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import type { TransactionResponse } from '../../api/types';
import './TransactionDetail.css';

/**
 * Detailed view of a single transaction.
 */
export function TransactionDetail() {
  const { transactionId } = useParams<{ transactionId: string }>();
  const navigate = useNavigate();
  const { apiClient, userId } = useApi();
  const [transaction, setTransaction] = useState<TransactionResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchTransaction = async () => {
      try {
        if (!transactionId || !userId) return;
        
        const data = await apiClient.transactions.getTransaction(transactionId);
        setTransaction(data);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load transaction');
      } finally {
        setLoading(false);
      }
    };

    fetchTransaction();
  }, [transactionId, userId, apiClient]);

  if (loading) return <div className="loading">Loading transaction...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!transaction) return <div className="error">Transaction not found</div>;

  return (
    <div className="transaction-detail">
      <button onClick={() => navigate('/transactions')} className="back-button">
        ← Back to Transactions
      </button>

      <div className="detail-card">
        <h2>{transaction.merchantName || 'Unknown Merchant'}</h2>
        
        <div className="amount-display">
          <span className={`amount ${transaction.amount < 0 ? 'negative' : 'positive'}`}>
            {transaction.amount < 0 ? '-' : '+'}${Math.abs(transaction.amount).toFixed(2)}
          </span>
        </div>

        <div className="detail-grid">
          <div className="detail-item">
            <label>Date</label>
            <span>{new Date(transaction.transactionDate).toLocaleDateString()}</span>
          </div>
          <div className="detail-item">
            <label>Category</label>
            <span>{transaction.categoryName || 'Uncategorized'}</span>
          </div>
          <div className="detail-item">
            <label>Type</label>
            <span>{transaction.transactionType || 'Unknown'}</span>
          </div>
          <div className="detail-item">
            <label>Confidence</label>
            <span>{transaction.confidenceScore ? `${(transaction.confidenceScore * 100).toFixed(0)}%` : 'N/A'}</span>
          </div>
        </div>

        {transaction.rawDescription && (
          <div className="detail-section">
            <label>Description</label>
            <p>{transaction.rawDescription}</p>
          </div>
        )}

        {transaction.llmReasoning && (
          <div className="detail-section">
            <label>Classification Reasoning</label>
            <p>{transaction.llmReasoning}</p>
          </div>
        )}

        {transaction.notes && (
          <div className="detail-section">
            <label>Notes</label>
            <p>{transaction.notes}</p>
          </div>
        )}

        {transaction.isRecurring && (
          <div className="recurring-badge">
            <span>🔄 Recurring {transaction.recurringFrequency ? `(${transaction.recurringFrequency})` : ''}</span>
          </div>
        )}
      </div>
    </div>
  );
}