import React from 'react';
import { useNavigate } from 'react-router-dom';
import './QuickActions.css';

/**
 * Quick action buttons for common tasks.
 */
export function QuickActions() {
  const navigate = useNavigate();

  const actions = [
    {
      id: 'upload',
      icon: '📤',
      title: 'Upload Statement',
      description: 'Import bank statement PDF',
      onClick: () => navigate('/statements'),
    },
    {
      id: 'add-entry',
      icon: '➕',
      title: 'Add Manual Entry',
      description: 'Record transaction manually',
      onClick: () => navigate('/manual-entries'),
    },
    {
      id: 'view-transactions',
      icon: '💳',
      title: 'View Transactions',
      description: 'Browse all transactions',
      onClick: () => navigate('/transactions'),
    },
    {
      id: 'analytics',
      icon: '📊',
      title: 'Analytics',
      description: 'View spending insights',
      onClick: () => navigate('/analytics'),
    },
  ];

  return (
    <div className="quick-actions">
      <h3>Quick Actions</h3>
      <div className="actions-grid">
        {actions.map((action) => (
          <button
            key={action.id}
            className="action-card"
            onClick={action.onClick}
            title={action.description}
          >
            <div className="action-icon">{action.icon}</div>
            <div className="action-title">{action.title}</div>
          </button>
        ))}
      </div>
    </div>
  );
}