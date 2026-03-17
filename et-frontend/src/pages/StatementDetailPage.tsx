
import React from 'react';
import { StatementDetail } from '../components/Statements/StatementDetail';
import { useAuth } from '../hooks/useAuth';
import { ErrorBoundary } from '../components/Common/ErrorBoundary';
import './StatementDetailPage.css';

/**
 * Page wrapper for statement detail view.
 * Provides layout and authentication context.
 */
export function StatementDetailPage() {
  const { userId } = useAuth();

  if (!userId) {
    return (
      <div className="page-container error-page">
        <div className="error-message">
          <h2>Authentication Required</h2>
          <p>Please log in to view statement details.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container statement-detail-page">
      <ErrorBoundary>
        <StatementDetail />
      </ErrorBoundary>
    </div>
  );
}