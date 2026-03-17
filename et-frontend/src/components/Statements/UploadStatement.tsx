
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../../api';
import './UploadStatement.css';

export function UploadStatement() {
  const navigate = useNavigate();
  const [file, setFile] = useState<File | null>(null);
  const [bankName, setBankName] = useState('');
  const [accountType, setAccountType] = useState<string>('');
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      if (!selectedFile.type.includes('pdf')) {
        setError('Only PDF files are supported');
        setFile(null);
      } else if (selectedFile.size > 50 * 1024 * 1024) {
        setError('File size must be less than 50MB');
        setFile(null);
      } else {
        setFile(selectedFile);
        setError(null);
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!file) {
      setError('Please select a file');
      return;
    }

    try {
      setUploading(true);
      setError(null);

      const response = await apiClient.statements.uploadStatement(
        file,
        bankName || undefined,
        accountType || undefined
      );

      setSuccess(true);
      setFile(null);
      setBankName('');
      setAccountType('');

      // Redirect to statement detail after 2 seconds
      setTimeout(() => {
        navigate(`/statements/${response.id}`);
      }, 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="upload-container">
      <div className="upload-box">
        <h2>📤 Upload Bank Statement</h2>
        <p className="upload-description">
          Upload a PDF bank or credit card statement. Our AI will automatically extract and
          categorize your transactions.
        </p>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">✓ File uploaded successfully! Redirecting...</div>}

        <form onSubmit={handleSubmit} className="upload-form">
          <div className="form-group">
            <label htmlFor="file">Select PDF File</label>
            <div className="file-input-wrapper">
              <input
                id="file"
                type="file"
                accept=".pdf"
                onChange={handleFileChange}
                disabled={uploading}
                className="file-input"
              />
              <span className="file-label">
                {file ? file.name : 'Choose a PDF file...'}
              </span>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="bankName">Bank Name (Optional)</label>
              <input
                id="bankName"
                type="text"
                value={bankName}
                onChange={(e) => setBankName(e.target.value)}
                placeholder="e.g., Chase, Wells Fargo"
                disabled={uploading}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="accountType">Account Type (Optional)</label>
              <select
                id="accountType"
                value={accountType}
                onChange={(e) => setAccountType(e.target.value)}
                disabled={uploading}
                className="form-input"
              >
                <option value="">Select type...</option>
                <option value="checking">Checking</option>
                <option value="savings">Savings</option>
                <option value="credit_card">Credit Card</option>
              </select>
            </div>
          </div>

          <button
            type="submit"
            disabled={!file || uploading}
            className={`btn btn-primary btn-large ${uploading ? 'loading' : ''}`}
          >
            {uploading ? 'Uploading...' : 'Upload Statement'}
          </button>
        </form>

        <div className="upload-info">
          <h4>ℹ️ How it works:</h4>
          <ol>
            <li>Upload your bank statement PDF</li>
            <li>Our AI extracts and analyzes transactions</li>
            <li>Transactions are automatically categorized</li>
            <li>Review and edit categories as needed</li>
          </ol>
        </div>
      </div>
    </div>
  );
}