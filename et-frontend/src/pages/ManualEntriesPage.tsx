
import React, { useState } from 'react';
import { useApi } from '../hooks/useApi';
import './ManualEntriesPage.css';

export function ManualEntriesPage() {
  const { apiClient } = useApi();
  const [formData, setFormData] = useState({
    transactionDate: new Date().toISOString().split('T')[0],
    amount: 0,
    description: '',
    categoryId: '',
    transactionType: 'expense' as const,
    notes: '',
  });
  const [categories, setCategories] = React.useState<any[]>([]);
  const [submitting, setSubmitting] = React.useState(false);
  const [message, setMessage] = React.useState('');

  React.useEffect(() => {
    apiClient.categories.getAllCategories().then(setCategories);
  }, []);

  const handleChange = (field: string, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.categoryId) {
      setMessage('Please select a category');
      return;
    }

    try {
      setSubmitting(true);
      await apiClient.manualEntries.createManualEntry(formData);
      setMessage('✓ Entry created successfully!');
      // Reset form
      setFormData({
        transactionDate: new Date().toISOString().split('T')[0],
        amount: 0,
        description: '',
        categoryId: '',
        transactionType: 'expense',
        notes: '',
      });
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      setMessage(`❌ Error: ${error}`);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="manual-entries-page">
      <h2>➕ Add Manual Entry</h2>

      {message && (
        <div className={`alert ${message.startsWith('✓') ? 'alert-success' : 'alert-error'}`}>
          {message}
        </div>
      )}

      <form onSubmit={handleSubmit} className="entry-form">
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="date">Date</label>
            <input
              id="date"
              type="date"
              value={formData.transactionDate}
              onChange={(e) => handleChange('transactionDate', e.target.value)}
              className="form-input"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="amount">Amount</label>
            <input
              id="amount"
              type="number"
              step="0.01"
              value={formData.amount}
              onChange={(e) => handleChange('amount', parseFloat(e.target.value))}
              placeholder="0.00"
              className="form-input"
              required
            />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <input
            id="description"
            type="text"
            value={formData.description}
            onChange={(e) => handleChange('description', e.target.value)}
            placeholder="e.g., Coffee at Starbucks"
            className="form-input"
            required
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="category">Category</label>
            <select
              id="category"
              value={formData.categoryId}
              onChange={(e) => handleChange('categoryId', e.target.value)}
              className="form-input"
              required
            >
              <option value="">Select a category...</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.icon} {cat.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="type">Type</label>
            <select
              id="type"
              value={formData.transactionType}
              onChange={(e) => handleChange('transactionType', e.target.value)}
              className="form-input"
            >
              <option value="expense">Expense</option>
              <option value="income">Income</option>
              <option value="transfer">Transfer</option>
            </select>
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="notes">Notes (Optional)</label>
          <textarea
            id="notes"
            value={formData.notes}
            onChange={(e) => handleChange('notes', e.target.value)}
            placeholder="Add any notes..."
            className="form-input"
            rows={3}
          />
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="btn btn-primary btn-large"
        >
          {submitting ? 'Saving...' : 'Save Entry'}
        </button>
      </form>
    </div>
  );
}