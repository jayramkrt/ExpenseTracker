import React from 'react';
import { Dashboard } from '../components/Dashboard/Dashboard';
import { QuickActions } from '../components/Dashboard/QuickActions';

export function DashboardPage() {
  return (
    <div>
      <QuickActions />
      <Dashboard />
    </div>
  );
}