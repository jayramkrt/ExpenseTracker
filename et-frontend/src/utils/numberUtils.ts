
/**
 * Number utility functions.
 */

export const numberUtils = {
  /**
   * Format amount as currency.
   */
  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  },

  /**
   * Format number with commas.
   */
  formatNumber(num: number, decimals: number = 2): string {
    return parseFloat(num.toFixed(decimals)).toLocaleString('en-US', {
      minimumFractionDigits: decimals,
      maximumFractionDigits: decimals,
    });
  },

  /**
   * Round to specified decimals.
   */
  round(num: number, decimals: number = 2): number {
    return parseFloat(num.toFixed(decimals));
  },

  /**
   * Calculate percentage.
   */
  percentage(part: number, whole: number): number {
    if (whole === 0) return 0;
    return this.round((part / whole) * 100, 2);
  },

  /**
   * Format percentage.
   */
  formatPercentage(num: number): string {
    return `${this.round(num, 1)}%`;
  },
};
