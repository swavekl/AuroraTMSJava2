package com.auroratms.paymentrefund;

/**
 * This determines whom we pay to and for what.  Each type must have a unique id
 * so for example club affiliation with id 123 can't be mixed up with tournament sanction fee with same id
 */
public enum PaymentRefundFor {
    TOURNAMENT_ENTRY,   // tournament entry
    CLINIC,             // a clinic
    CLUB_AFFILIATION_FEE,    // payment to USATT
    TOURNAMENT_SANCTION_FEE,    // payment to USATT
    MEMBERSHIP_FEE    // payment to USATT
}
