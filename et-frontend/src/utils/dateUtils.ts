
/**
 * Date utility functions.
 */

export const dateUtils = {
  /**
   * Format date to string (YYYY-MM-DD).
   */
  formatDate(date: Date | string): string {
    if (typeof date === 'string') return date;
    return date.toISOString().split('T')[0];
  },

  /**
   * Format date for display.
   */
  formatDisplayDate(date: Date | string): string {
    const d = typeof date === 'string' ? new Date(date) : date;
    return d.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  },

  /**
   * Get start of month.
   */
  getStartOfMonth(date: Date = new Date()): Date {
    return new Date(date.getFullYear(), date.getMonth(), 1);
  },

  /**
   * Get end of month.
   */
  getEndOfMonth(date: Date = new Date()): Date {
    return new Date(date.getFullYear(), date.getMonth() + 1, 0);
  },

  /**
   * Get date N days ago.
   */
  getDaysAgo(days: number): Date {
    const date = new Date();
    date.setDate(date.getDate() - days);
    return date;
  },

  /**
   * Check if date is today.
   */
  isToday(date: Date | string): boolean {
    const d = typeof date === 'string' ? new Date(date) : date;
    const today = new Date();
    return d.toDateString() === today.toDateString();
  },
};
