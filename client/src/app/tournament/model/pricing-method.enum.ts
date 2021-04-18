/**
 * Pricing method of tournament entry
 */
export enum PricingMethod {
  // just add up all the costs
  STANDARD = 'STANDARD',

  // add up costs but apply discount based on say number of events entered
  DISCOUNTED = 'DISCOUNTED',

  // group tournaments have special pricing
  GROUP = 'GROUP'
}

