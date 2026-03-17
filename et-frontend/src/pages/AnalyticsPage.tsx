import React, { useState } from 'react';
import { useAnalytics } from '../hooks/useAnalytics';
import { Dashboard } from '../components/Dashboard/Dashboard';

export function AnalyticsPage() {
  return (
    <div>
      <h2>📈 Analytics & Insights</h2>
      <Dashboard />
    </div>
  );
}