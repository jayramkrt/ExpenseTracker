
/**
 * Application constants.
 */

export const API_STATUS = {
  PENDING: 'pending',
  PROCESSING: 'processing',
  COMPLETED: 'completed',
  FAILED: 'failed',
} as const;

export const TRANSACTION_TYPES = {
  DEBIT: 'debit',
  CREDIT: 'credit',
  TRANSFER: 'transfer',
} as const;

export const DEFAULT_PAGINATION = {
  PAGE_SIZE: 20,
  MAX_PAGE_SIZE: 100,
} as const;

export const STORAGE_KEYS = {
  USER_ID: 'userId',
  AUTH_TOKEN: 'authToken',
  THEME: 'theme',
} as const;

export const CATEGORIES = [
  'Groceries',
  'Utilities',
  'Entertainment',
  'Transportation',
  'Healthcare',
  'Salary/Income',
  'Subscriptions',
  'Shopping',
  'Dining & Restaurants',
  'Insurance',
  'Education',
  'Transfer',
  'Other',
] as const;
