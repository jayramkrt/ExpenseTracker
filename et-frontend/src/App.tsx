
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Layout } from './components/Layout/Layout';
import { ErrorBoundary } from './components/Common/ErrorBoundary';

// Import all pages
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { StatementsPage } from './pages/StatementsPage';
import { StatementDetailPage } from './pages/StatementDetailPage';
import { TransactionsPage } from './pages/TransactionsPage';
import { TransactionDetailPage } from './pages/TransactionDetailPage';
import { ManualEntriesPage } from './pages/ManualEntriesPage';
import { ManualEntryDetailPage } from './pages/ManualEntryDetailPage';
import { AnalyticsPage } from './pages/AnalyticsPage';

import './App.css';

/**
 * Protected route wrapper - redirects to login if not authenticated.
 */
function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { userId } = useAuth();

  if (!userId) {
    return <Navigate to="/login" replace />;
  }

  return <Layout>{children}</Layout>;
}

/**
 * Main App component with routing.
 */
function AppRoutes() {
  const { userId } = useAuth();

  return (
    <Routes>
      {/* Public Routes */}
      <Route
        path="/login"
        element={!userId ? <LoginPage /> : <Navigate to="/" replace />}
      />

      {/* Protected Routes */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />

      {/* Statements Routes */}
      <Route
        path="/statements"
        element={
          <ProtectedRoute>
            <StatementsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/statements/:statementId"
        element={
          <ProtectedRoute>
            <StatementDetailPage />
          </ProtectedRoute>
        }
      />

      {/* Transactions Routes */}
      <Route
        path="/transactions"
        element={
          <ProtectedRoute>
            <TransactionsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/transactions/:transactionId"
        element={
          <ProtectedRoute>
            <TransactionDetailPage />
          </ProtectedRoute>
        }
      />

      {/* Manual Entries Routes */}
      <Route
        path="/manual-entries"
        element={
          <ProtectedRoute>
            <ManualEntriesPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/manual-entries/:entryId"
        element={
          <ProtectedRoute>
            <ManualEntryDetailPage />
          </ProtectedRoute>
        }
      />

      {/* Analytics Route */}
      <Route
        path="/analytics"
        element={
          <ProtectedRoute>
            <AnalyticsPage />
          </ProtectedRoute>
        }
      />

      {/* Catch all - redirect to home */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

/**
 * Main App component.
 */
export function App() {
  return (
    <ErrorBoundary fallback={<div className="app-error">Application Error</div>}>
      <Router>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </Router>
    </ErrorBoundary>
  );
}

export default App;
