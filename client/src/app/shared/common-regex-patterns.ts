/**
 * Commonly used patterns for validation
 */

export class CommonRegexPatterns {

  public static readonly PRICE_REGEX = /^\d{1,4}([\.,]\d{2})?$/;

  public static readonly NUMERIC_WITH_ZERO_REGEX   = /^[0-9]$/;
  public static readonly NUMERIC_REGEX             = /^[1-9]$/;
  public static readonly TWO_DIGIT_NUMERIC_REGEX   = /^[1-9]\d?$/;
  public static readonly THREE_DIGIT_NUMERIC_REGEX = /^[1-9](\d{1,2})?$/;
  public static readonly FOUR_DIGIT_NUMERIC_REGEX  = /^[1-9](\d{1,3})?$/;
  public static readonly FIVE_DIGIT_NUMERIC_REGEX  = /^[1-9](\d{1,4})?$/;

}
