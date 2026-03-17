
import React, { useState } from 'react';
import { useTransactions } from '../../hooks/useTransactions';
import { useApi } from '../../hooks/useApi';
import { TransactionRow } from './TransactionRow';
import './TransactionsList.css';

export function TransactionsList() {
  const [startDate, setStartDate] = useState<string>();
  const [endDate, setEndDate] = useState<string>();
  const [categoryId, setCategoryId] = useState<string>();
  const [searchQuery, setSearchQuery] = useState('');

  const { transactions, loading, error, page, setPage, totalPages, searchTransactions } =
    useTransactions(categoryId, startDate, endDate);

  const { apiClient } = useApi();
  const [categories, setCategories] = React.useState<any[]>([]);

  React.useEffect(() => {
    apiClient.categories.getAllCategories().then(setCategories).catch(console.error);
  }, []);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      await searchTransactions(searchQuery);
    }
  };

  return (
    <div className="transactions-container">
      <h2>💳 Transactions</h2>

      {error && <div className="alert alert-error">Error: {error.message}</div>}

      <div className="filters-section">
        <form onSubmit={handleSearch} className="search-form">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by merchant or description..."
            className="search-input"
          />
          <button type="submit" className="btn btn-secondary">
            Search
          </button>
        </form>

        <div className="filter-group">
          <select
            value={categoryId || ''}
            onChange={(e) => setCategoryId(e.target.value || undefined)}
            className="filter-select"
          >
            <option value="">All Categories</option>
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {cat.icon} {cat.name}
              </option>
            ))}
          </select>

          <input
            type="date"
            value={startDate || ''}
            onChange={(e) => setStartDate(e.target.value || undefined)}
            className="filter-input"
            placeholder="Start Date"
          />

          <input
            type="date"
            value={endDate || ''}
            onChange={(e) => setEndDate(e.target.value || undefined)}
            className="filter-input"
            placeholder="End Date"
          />
        </div>
      </div>

      {loading ? (
        <div className="loading">Loading transactions...</div>
      ) : transactions.length === 0 ? (
        <div className="no-data">
          <p>No transactions found</p>
        </div>
      ) : (
        <>
          <table className="transactions-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Merchant</th>
                <th>Description</th>
                <th>Amount</th>
                <th>Category</th>
                <th>Confidence</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((txn) => (
                <TransactionRow key={txn.id} transaction={txn} categories={categories} />
              ))}
            </tbody>
          </table>

          <div className="pagination">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
              className="btn btn-secondary"
            >
              ← Previous
            </button>
            <span className="page-info">
              Page {page + 1} of {totalPages}
            </span>
            <button
              onClick={() => setPage(page + 1)}
              disabled={page >= totalPages - 1}
              className="btn btn-secondary"
            >
              Next →
            </button>
          </div>
        </>
      )}
    </div>
  );
}