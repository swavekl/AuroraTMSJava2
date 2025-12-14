package com.auroratms.event;

/**
 * Method of payment
 */
public enum FeeStructure {
    // one fee regardless of time of entry - most common
    FIXED,

    // changing according to a schedule - usually used at teams tournaments
    PER_SCHEDULE,

    // package 7 events $250 - used at US Nationals and US Open
    // determined at the tournament level
    PACKAGE_DISCOUNT
}

