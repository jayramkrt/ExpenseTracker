import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import './Layout.css';

interface LayoutProps {
  children: React.ReactNode;
}

export function Layout({ children }: LayoutProps) {
  const navigate = useNavigate();
  const { userId, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="layout">
      <nav className="navbar">
        <div className="navbar-brand">
          <h1>💰 Finance Analyzer</h1>
        </div>

        <div className="nav-links">
          <a href="/" className="nav-link">Dashboard</a>
          <a href="/statements" className="nav-link">Statements</a>
          <a href="/transactions" className="nav-link">Transactions</a>
          <a href="/manual-entries" className="nav-link">Add Entry</a>
          <a href="/analytics" className="nav-link">Analytics</a>
        </div>

        <div className="navbar-right">
          <span className="user-id">{userId}</span>
          <button onClick={handleLogout} className="btn btn-logout">
            Logout
          </button>
        </div>
      </nav>

      <div className="layout-body">
        <main className="main-content">
          {children}
        </main>
      </div>

      <footer className="footer">
        <p>&copy; 2024 Personal Finance Analyzer. All data is private and local.</p>
      </footer>
    </div>
  );
}