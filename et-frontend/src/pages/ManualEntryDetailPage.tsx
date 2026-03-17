import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../hooks/useAuth';
import type { ManualEntryResponse } from '../api/types';
import { ErrorBoundary } from '../components/Common/ErrorBoundary';
import { LoadingSpinner } from '../components/Common/LoadingSpinner';
import './ManualEntryDetailPage.css';

/**
 * Page for viewing and editing a manual entry.
 */
export function ManualEntryDetailPage() {
  const { entryId } = useParams<{ entryId: string }>();
  const navigate = useNavigate();
  const { userId } = useAuth();
  const { apiClient } = useApi();

  const [entry, setEntry] = useState<ManualEntryResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isEditing, setIsEditing] = useState(false);

  useEffect(() => {
    const fetchEntry = async () => {
      try {
        if (!entryId || !userId) return;

        // Note: You may need to add this method to the API client
        // For now, this is a placeholder
        const data = await apiClient.manualEntries.getManualEntry(entryId);
        setEntry(data);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load entry');
      } finally {
        setLoading(false);
      }
    };

    fetchEntry();
  }, [entryId, userId, apiClient]);

  if (!userId) {
    return (
      <div className="page-container error-page">
        <div className="error-message">
          <h2>Authentication Required</h2>
          <p>Please log in to view this entry.</p>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="page-container">
        <LoadingSpinner message="Loading entry..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-container error-page">
        <div className="error-message">
          <h2>Error</h2>
          <p>{error}</p>
          <button onClick={() => navigate('/manual-entries')} className="btn btn-primary">
            Back to Entries
          </button>
        </div>
      </div>
    );
  }

  if (!entry) {
    return (
      <div className="page-container error-page">
        <div className="error-message">
          <h2>Entry Not Found</h2>
          <p>The entry you're looking for doesn't exist.</p>
          <button onClick={() => navigate('/manual-entries')} className="btn btn-primary">
            Back to Entries
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container manual-entry-detail-page">
      <ErrorBoundary>
        <div className="entry-detail-card">
          <div className="entry-header">
            <h2>{entry.description}</h2>
            <button onClick={() => navigate('/manual-entries')} className="back-button">
              ← Back
            </button>
          </div>

          <div className="entry-amount">
            <span className={`amount ${entry.amount < 0 ? 'negative' : 'positive'}`}>
              {entry.amount < 0 ? '-' : '+'}${Math.abs(entry.amount).toFixed(2)}
            </span>
          </div>

          <div className="entry-grid">
            <div className="entry-item">
              <label>Date</label>
              <span>{new Date(entry.transactionDate).toLocaleDateString()}</span>
            </div>
            <div className="entry-item">
              <label>Category</label>
              <span>{entry.categoryName || 'Uncategorized'}</span>
            </div>
            <div className="entry-item">
              <label>Type</label>
              <span>{entry.transactionType}</span>
            </div>
          </div>

          {entry.notes && (
            <div className="entry-section">
              <label>Notes</label>
              <p>{entry.notes}</p>
            </div>
          )}

          <div className="action-buttons">
            <button
              onClick={() => setIsEditing(!isEditing)}
              className="btn btn-primary"
            >
              {isEditing ? 'Cancel' : 'Edit'}
            </button>
            <button className="btn btn-danger">Delete Entry</button>
          </div>
        </div>
      </ErrorBoundary>
    </div>
  );
}