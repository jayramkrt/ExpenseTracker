
/**
 * Form validation utility functions.
 */

export const validation = {
  /**
   * Validate email.
   */
  isValidEmail(email: string): boolean {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
  },

  /**
   * Validate amount.
   */
  isValidAmount(amount: any): boolean {
    const num = parseFloat(amount);
    return !isNaN(num) && num !== 0;
  },

  /**
   * Validate date.
   */
  isValidDate(date: any): boolean {
    if (!date) return false;
    const d = new Date(date);
    return d instanceof Date && !isNaN(d.getTime());
  },

  /**
   * Validate date range.
   */
  isValidDateRange(startDate: any, endDate: any): boolean {
    if (!this.isValidDate(startDate) || !this.isValidDate(endDate)) return false;
    return new Date(startDate) <= new Date(endDate);
  },

  /**
   * Validate UUID.
   */
  isValidUUID(uuid: string): boolean {
    const regex =
      /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
    return regex.test(uuid);
  },

  /**
   * Validate required field.
   */
  isRequired(value: any): boolean {
    if (typeof value === 'string') return value.trim().length > 0;
    return value !== null && value !== undefined;
  },

  /**
   * Validate min length.
   */
  minLength(value: string, min: number): boolean {
    return value.length >= min;
  },

  /**
   * Validate max length.
   */
  maxLength(value: string, max: number): boolean {
    return value.length <= max;
  },
};
