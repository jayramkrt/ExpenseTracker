
import React, { useState } from 'react';
import { useStatements } from '../hooks/useStatements';
import { UploadStatement } from '../components/Statements/UploadStatement';
import { StatementsList } from '../components/Statements/StatementsList';
import './StatementsPage.css';

export function StatementsPage() {
  const [showUpload, setShowUpload] = useState(false);

  return (
    <div className="statements-page">
      <div className="page-header">
        <h2>📄 Bank Statements</h2>
        <button
          onClick={() => setShowUpload(!showUpload)}
          className="btn btn-primary"
        >
          {showUpload ? 'Cancel' : '+ Upload New Statement'}
        </button>
      </div>

      {showUpload && (
        <div className="upload-section">
          <UploadStatement />
        </div>
      )}

      <StatementsList />
    </div>
  );
}