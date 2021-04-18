package com.auroratms.tournament;

/**
 * Pricing method of tournament entry
 */
public enum PricingMethod {
    // just add up all the costs
    STANDARD,

    // add up costs but apply discount based on say number of events entered
    DISCOUNTED,

    // group tournaments have special pricing
    GROUP
}
