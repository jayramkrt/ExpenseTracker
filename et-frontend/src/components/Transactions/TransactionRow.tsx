
import React, { useState } from 'react';
import { apiClient } from '../../api';
import type { TransactionResponse } from '../../api/types';
//import './TransactionRow.css';

interface TransactionRowProps {
  transaction: TransactionResponse;
  categories: any[];
}

export function TransactionRow({ transaction, categories }: TransactionRowProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [categoryId, setCategoryId] = useState(transaction.categoryId);
  const [notes, setNotes] = useState(transaction.notes || '');
  const [updating, setUpdating] = useState(false);

  const handleSave = async () => {
    try {
      setUpdating(true);
      await apiClient.transactions.updateTransaction(transaction.id, {
        categoryId: categoryId,
        notes: notes,
      });
      setIsEditing(false);
      // Trigger refresh (would need to implement callback)
    } catch (error) {
      console.error('Update failed:', error);
    } finally {
      setUpdating(false);
    }
  };

  const confidenceColor = (score?: number) => {
    if (!score) return '#999';
    if (score >= 0.9) return '#27AE60';
    if (score >= 0.7) return '#F39C12';
    return '#E74C3C';
  };

  return (
    <tr className={`transaction-row ${transaction.isManual ? 'manual' : ''}`}>
      <td className="transaction-date">{transaction.transactionDate}</td>
      <td className="transaction-merchant">{transaction.merchantName || '-'}</td>
      <td className="transaction-description" title={transaction.rawDescription}>
        {transaction.rawDescription?.substring(0, 40)}...
      </td>
      <td className="transaction-amount">
        <span className={transaction.amount < 0 ? 'negative' : 'positive'}>
          ${Math.abs(transaction.amount).toFixed(2)}
        </span>
      </td>
      <td className="transaction-category">
        {isEditing ? (
          <select
            value={categoryId || ''}
            onChange={(e) => setCategoryId(e.target.value)}
            className="edit-select"
          >
            <option value="">Select...</option>
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {cat.icon} {cat.name}
              </option>
            ))}
          </select>
        ) : (
          <span className="category-badge">
            {transaction.categoryName}
          </span>
        )}
      </td>
      <td className="transaction-confidence">
        <span
          className="confidence-badge"
          style={{ backgroundColor: confidenceColor(transaction.confidenceScore) }}
        >
          {transaction.confidenceScore ? `${(transaction.confidenceScore * 100).toFixed(0)}%` : 'N/A'}
        </span>
      </td>
      <td className="transaction-actions">
        {isEditing ? (
          <>
            <button
              onClick={handleSave}
              disabled={updating}
              className="btn btn-small btn-success"
            >
              Save
            </button>
            <button
              onClick={() => setIsEditing(false)}
              className="btn btn-small btn-secondary"
            >
              Cancel
            </button>
          </>
        ) : (
          <button onClick={() => setIsEditing(true)} className="btn btn-small btn-primary">
            Edit
          </button>
        )}
      </td>
    </tr>
  );
}
