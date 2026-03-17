import React from 'react';
import './Common.css';

interface LoadingSpinnerProps {
  message?: string;
  size?: 'small' | 'medium' | 'large';
}

/**
 * Loading spinner component.
 */
export function LoadingSpinner({ message = 'Loading...', size = 'medium' }: LoadingSpinnerProps) {
  return (
    <div className={`loading-spinner ${size}`}>
      <div className="spinner"></div>
      {message && <p>{message}</p>}
    </div>
  );
}