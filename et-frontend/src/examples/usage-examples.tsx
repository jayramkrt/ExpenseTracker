
/**
 * Example 1: Upload Statement Component
 */
import React, { useState } from 'react';
import { useApi } from '../hooks/useApi';

export function UploadStatementExample() {
  const { apiClient } = useApi();
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState<string | null>(null);

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) return;

    try {
      setUploading(true);
      const response = await apiClient.statements.uploadStatement(
        file,
        'Chase',
        'checking'
      );
      setResult(`Uploaded! Statement ID: ${response.id}`);
      setFile(null);
    } catch (error) {
      setResult(`Error: ${error}`);
    } finally {
      setUploading(false);
    }
  };

  return (
    <form onSubmit={handleUpload}>
      <input
        type="file"
        accept=".pdf"
        onChange={(e) => setFile(e.target.files?.[0] || null)}
      />
      <button type="submit" disabled={!file || uploading}>
        {uploading ? 'Uploading...' : 'Upload'}
      </button>
      {result && <p>{result}</p>}
    </form>
  );
}

/**
 * Example 2: Transactions List with Filtering
 */
export function TransactionsListExample() {
  const { useTransactions } = useApi();
  const [categoryId, setCategoryId] = useState<string>();
  const [startDate, setStartDate] = useState<string>();
  const [endDate, setEndDate] = useState<string>();

  const { transactions, loading, page, setPage, totalPages, updateTransaction } =
    useTransactions(categoryId, startDate, endDate);

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <div className="filters">
        <input
          type="date"
          value={startDate || ''}
          onChange={(e) => setStartDate(e.target.value)}
          placeholder="Start Date"
        />
        <input
          type="date"
          value={endDate || ''}
          onChange={(e) => setEndDate(e.target.value)}
          placeholder="End Date"
        />
      </div>

      <table>
        <thead>
          <tr>
            <th>Date</th>
            <th>Merchant</th>
            <th>Amount</th>
            <th>Category</th>
            <th>Confidence</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((txn) => (
            <tr key={txn.id}>
              <td>{txn.transactionDate}</td>
              <td>{txn.merchantName}</td>
              <td>${txn.amount.toFixed(2)}</td>
              <td>{txn.categoryName}</td>
              <td>{(txn.confidenceScore || 0).toFixed(2)}</td>
              <td>
                <button
                  onClick={() =>
                    updateTransaction(txn.id, {
                      categoryId: txn.categoryId,
                      notes: 'Updated',
                    })
                  }
                >
                  Edit
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="pagination">
        <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0}>
          Previous
        </button>
        <span>
          Page {page + 1} of {totalPages}
        </span>
        <button
          onClick={() => setPage(page + 1)}
          disabled={page >= totalPages - 1}
        >
          Next
        </button>
      </div>
    </div>
  );
}

/**
 * Example 3: Analytics Dashboard
 */
export function AnalyticsDashboardExample() {
  const { analytics } = useApi();
  const { overview, categoryBreakdown, monthlyTrends, topMerchants, isLoading } =
    analytics();

  if (isLoading) return <div>Loading analytics...</div>;

  return (
    <div className="analytics-dashboard">
      <div className="summary">
        <div className="card">
          <h3>Total Income</h3>
          <p>${overview?.totalIncome.toFixed(2)}</p>
        </div>
        <div className="card">
          <h3>Total Expenses</h3>
          <p>${Math.abs(overview?.totalExpenses || 0).toFixed(2)}</p>
        </div>
        <div className="card">
          <h3>Net Balance</h3>
          <p>${overview?.netBalance.toFixed(2)}</p>
        </div>
      </div>

      <div className="charts">
        <div className="category-breakdown">
          <h3>Spending by Category</h3>
          <ul>
            {categoryBreakdown?.map((cat) => (
              <li key={cat.categoryId}>
                <span>{cat.categoryName}</span>
                <span>{cat.percentage.toFixed(1)}%</span>
              </li>
            ))}
          </ul>
        </div>

        <div className="monthly-trends">
          <h3>Monthly Trends</h3>
          <table>
            <thead>
              <tr>
                <th>Month</th>
                <th>Income</th>
                <th>Expenses</th>
                <th>Net</th>
              </tr>
            </thead>
            <tbody>
              {monthlyTrends?.map((trend) => (
                <tr key={trend.month}>
                  <td>{trend.month}</td>
                  <td>${trend.income.toFixed(2)}</td>
                  <td>${trend.expenses.toFixed(2)}</td>
                  <td>${trend.netBalance.toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="top-merchants">
          <h3>Top Merchants</h3>
          <ol>
            {topMerchants?.map((m) => (
              <li key={m.merchantName}>
                {m.merchantName}: ${m.totalSpending.toFixed(2)} ({m.transactionCount} txns)
              </li>
            ))}
          </ol>
        </div>
      </div>
    </div>
  );
}

/**
 * Example 4: Search Transactions
 */
export function SearchTransactionsExample() {
  const { transactions } = useApi();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!query) return;

    try {
      const data = await transactions().searchTransactions(query);
      setResults(data);
    } catch (error) {
      console.error('Search failed:', error);
    }
  };

  return (
    <div>
      <form onSubmit={handleSearch}>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search merchant or description..."
        />
        <button type="submit">Search</button>
      </form>

      <div className="results">
        {results.map((txn) => (
          <div key={txn.id} className="result-card">
            <p>
              <strong>{txn.merchantName}</strong> - ${txn.amount.toFixed(2)}
            </p>
            <p className="category">{txn.categoryName}</p>
            <p className="date">{txn.transactionDate}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

/**
 * Example 5: Create Manual Entry
 */
export function CreateManualEntryExample() {
  const { apiClient } = useApi();
  const [formData, setFormData] = useState({
    transactionDate: '',
    amount: 0,
    description: '',
    categoryId: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await apiClient.manualEntries.createManualEntry(formData);
      alert('Entry created successfully!');
      setFormData({
        transactionDate: '',
        amount: 0,
        description: '',
        categoryId: '',
      });
    } catch (error) {
      alert(`Error: ${error}`);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="date"
        required
        value={formData.transactionDate}
        onChange={(e) =>
          setFormData({ ...formData, transactionDate: e.target.value })
        }
      />
      <input
        type="number"
        step="0.01"
        required
        value={formData.amount}
        onChange={(e) =>
          setFormData({ ...formData, amount: parseFloat(e.target.value) })
        }
        placeholder="Amount"
      />
      <input
        type="text"
        required
        value={formData.description}
        onChange={(e) =>
          setFormData({ ...formData, description: e.target.value })
        }
        placeholder="Description"
      />
      <select
        required
        value={formData.categoryId}
        onChange={(e) =>
          setFormData({ ...formData, categoryId: e.target.value })
        }
      >
        <option value="">Select Category</option>
        {/* Populate with categories */}
      </select>
      <button type="submit">Create Entry</button>
    </form>
  );
}