
/**
 * Type definitions for all API requests and responses.
 */

// ==================== STATEMENTS ====================

export interface StatementResponse {
  id: string;
  filename: string;
  bankName?: string;
  accountType?: string;
  accountLastFour?: string;
  statementPeriodStart?: string;
  statementPeriodEnd?: string;
  processingStatus: 'pending' | 'processing' | 'completed' | 'failed';
  errorMessage?: string;
  transactionCount: number;
  uploadedAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface StatementUploadResponse {
  id: string;
  filename: string;
  processingStatus: string;
  transactionCount: number;
  uploadedAt: string;
}

export interface StatementStatusResponse {
  statementId: string;
  status: string;
  transactionCount: number;
  errorMessage?: string;
}

/*export interface PaginatedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  isFirst: boolean;
  isLast: boolean;
}*/

export interface PaginatedResponse<T> {
  content: T[];
  number: number;       // Spring uses "number" not "pageNumber"
  size: number;         // Spring uses "size" not "pageSize"
  totalElements: number;
  totalPages: number;
  first: boolean;       // Spring uses "first" not "isFirst"
  last: boolean;        // Spring uses "last" not "isLast"
}

// ==================== TRANSACTIONS ====================

export interface TransactionResponse {
  id: string;
  transactionDate: string;
  amount: number;
  rawDescription?: string;
  merchantName?: string;
  referenceNumber?: string;
  categoryId?: string;
  categoryName?: string;
  subcategory?: string;
  confidenceScore?: number;
  llmReasoning?: string;
  transactionType?: 'debit' | 'credit' | 'transfer';
  isManual: boolean;
  isRecurring: boolean;
  recurringFrequency?: string;
  notes?: string;
  statementId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TransactionUpdateRequest {
  categoryId?: string;
  categoryName?: string;
  notes?: string;
}

// ==================== MANUAL ENTRIES ====================

export interface ManualEntryRequest {
  transactionDate: string;
  amount: number;
  description: string;
  categoryId: string;
  transactionType?: 'income' | 'expense' | 'transfer';
  notes?: string;
}

export interface ManualEntryResponse {
  id: string;
  transactionDate: string;
  amount: number;
  description: string;
  categoryId: string;
  categoryName?: string;
  transactionType?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

// ==================== CATEGORIES ====================

export interface CategoryResponse {
  id: string;
  name: string;
  description?: string;
  icon: string;
  color: string;
  isSystem: boolean;
}

// ==================== ANALYTICS ====================

export interface AnalyticsResponse {
  totalIncome: number;
  totalExpenses: number;
  netBalance: number;
  averageMonthlyIncome?: number;
  averageMonthlyExpenses?: number;
  transactionCount: number;
  manualEntryCount?: number;
  monthlyTrends?: MonthlyTrendResponse[];
  categoryBreakdown?: CategoryBreakdownResponse[];
}

export interface CategoryBreakdownResponse {
  categoryId: string;
  categoryName: string;
  icon?: string;
  color?: string;
  totalAmount: number;
  percentage: number;
  transactionCount: number;
  averagePerTransaction: number;
}

export interface MonthlyTrendResponse {
  month: string; // "2024-01"
  income: number;
  expenses: number;
  netBalance: number;
  transactionCount: number;
}

export interface TopMerchantResponse {
  merchantName: string;
  totalSpending: number;
  transactionCount: number;
  averagePerTransaction?: number;
}

export interface RecurringTransactionResponse {
  transactionId: string;
  merchantName: string;
  amount: number;
  frequency: string;
  lastOccurrence: string;
  occurrenceCount: number;
}

export interface AnomalyResponse {
  transactionId?: string;
  merchantName?: string;
  amount: number;
  categoryName?: string;
  date: string;
  deviation: number;
  reason?: string;
}

// ==================== ERRORS ====================

export interface ErrorResponse {
  timestamp: string;
  status: number;
  message: string;
  error: string;
  fieldErrors?: Record<string, string>;
}