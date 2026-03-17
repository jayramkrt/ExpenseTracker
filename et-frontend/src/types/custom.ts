
/**
 * Custom TypeScript type definitions not from API.
 */

export interface NavigationItem {
  path: string;
  label: string;
  icon: string;
}

export interface FilterOptions {
  categoryId?: string;
  startDate?: string;
  endDate?: string;
  merchantName?: string;
  isManual?: boolean;
}

export interface PaginationParams {
  page: number;
  size: number;
}

export interface ToastMessage {
  id: string;
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
  duration?: number;
}

export interface MenuItem {
  id: string;
  label: string;
  icon?: string;
  onClick: () => void;
  disabled?: boolean;
}