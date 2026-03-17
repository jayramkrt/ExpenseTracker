
import React, { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { apiClient } from '../api';

/**
 * Authentication context type.
 */
export interface AuthContextType {
  userId: string | null;
  isAuthenticated: boolean;
  login: (userId: string) => void;
  logout: () => void;
}

/**
 * Create the auth context.
 */
export const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * Auth provider props.
 */
interface AuthProviderProps {
  children: ReactNode;
}

/**
 * Authentication provider component.
 * Manages user authentication state and persistence.
 */
export function AuthProvider({ children }: AuthProviderProps) {
  const [userId, setUserId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Initialize from localStorage on mount
  useEffect(() => {
    try {
      const storedUserId = localStorage.getItem('userId');
      if (storedUserId) {
        setUserId(storedUserId);
        // Set userId in API client
        apiClient.setUserId(storedUserId);
      }
    } catch (error) {
      console.error('Failed to load user from storage:', error);
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Login - set user ID and persist.
   */
  const login = (id: string) => {
    try {
      setUserId(id);
      localStorage.setItem('userId', id);
      apiClient.setUserId(id);
    } catch (error) {
      console.error('Failed to login:', error);
    }
  };

  /**
   * Logout - clear user ID and auth.
   */
  const logout = () => {
    try {
      setUserId(null);
      localStorage.removeItem('userId');
      //apiClient.clearUserId();
    } catch (error) {
      console.error('Failed to logout:', error);
    }
  };

  const value: AuthContextType = {
    userId,
    isAuthenticated: !!userId,
    login,
    logout,
  };

  // Don't render children until auth state is initialized
  if (isLoading) {
    return (
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          backgroundColor: '#f5f5f5',
        }}
      >
        <div style={{ textAlign: 'center' }}>
          <div
            style={{
              width: '40px',
              height: '40px',
              border: '4px solid #e0e0e0',
              borderTop: '4px solid #667eea',
              borderRadius: '50%',
              animation: 'spin 0.8s linear infinite',
              margin: '0 auto 1rem',
            }}
          />
          <p style={{ color: '#666', margin: 0 }}>Loading...</p>
          <style>{`
            @keyframes spin {
              to { transform: rotate(360deg); }
            }
          `}</style>
        </div>
      </div>
    );
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * Hook to use auth context.
 * Must be used within AuthProvider.
 */
export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  
  if (context === undefined) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  
  return context;
}

// Export for convenience
export default AuthContext;
