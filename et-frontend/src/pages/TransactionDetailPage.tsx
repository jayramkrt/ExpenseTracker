import React from 'react';
import { TransactionDetail } from '../components/Transactions/TransactionDetail';
import { useAuth } from '../hooks/useAuth';
import { ErrorBoundary } from '../components/Common/ErrorBoundary';
import './TransactionDetailPage.css';

/**
 * Page wrapper for transaction detail view.
 * Provides layout and authentication context.
 */
export function TransactionDetailPage() {
  const { userId } = useAuth();

  if (!userId) {
    return (
      <div className="page-container error-page">
        <div className="error-message">
          <h2>Authentication Required</h2>
          <p>Please log in to view transaction details.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container transaction-detail-page">
      <ErrorBoundary>
        <TransactionDetail />
      </ErrorBoundary>
    </div>
  );
}