
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './LoginPage.css';

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [userId, setUserId] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!userId.trim()) {
      setError('Please enter a User ID');
      return;
    }

    // In a real app, this would validate against a backend
    // For demo, we accept any non-empty user ID
    login(userId);
    navigate('/');
  };

  return (
    <div className="login-page">
      <div className="login-container">
        <div className="login-box">
          <h1>💰 Personal Finance Analyzer</h1>
          <p className="login-subtitle">Analyze your spending with AI</p>

          <form onSubmit={handleSubmit} className="login-form">
            <div className="form-group">
              <label htmlFor="userId">User ID</label>
              <input
                id="userId"
                type="text"
                value={userId}
                onChange={(e) => {
                  setUserId(e.target.value);
                  setError('');
                }}
                placeholder="Enter your user ID (UUID format)"
                className={`form-input ${error ? 'error' : ''}`}
                autoFocus
              />
              {error && <span className="error-message">{error}</span>}
            </div>

            <button type="submit" className="btn btn-primary btn-large">
              Login
            </button>
          </form>

          <div className="login-info">
            <h3>Demo Instructions:</h3>
            <p>
              Enter a UUID (e.g., <code>550e8400-e29b-41d4-a716-446655440000</code>)
              or any unique identifier to get started.
            </p>
            <p className="note">💡 All data is stored locally. No cloud sync.</p>
          </div>
        </div>

        <div className="login-features">
          <h2>Features</h2>
          <ul>
            <li>📤 Upload bank statements (PDF)</li>
            <li>🤖 AI-powered transaction classification</li>
            <li>📊 Advanced analytics & insights</li>
            <li>💳 Manual transaction entry</li>
            <li>📈 Monthly spending trends</li>
            <li>🔐 100% private (runs locally)</li>
          </ul>
        </div>
      </div>
    </div>
  );
}