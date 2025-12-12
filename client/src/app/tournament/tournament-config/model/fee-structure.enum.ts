/**
 * Method of payment
 */
export enum FeeStructure {
  // one fee regardless of time of entry - most common
  FIXED = 'FIXED',

  // changing according to a schedule - usually used at teams tournaments
  PER_SCHEDULE = 'PER_SCHEDULE',

  // package 7 events $250 - used at US Nationals and US Open
  PACKAGE_DISCOUNT = 'PACKAGE_DISCOUNT'
}
