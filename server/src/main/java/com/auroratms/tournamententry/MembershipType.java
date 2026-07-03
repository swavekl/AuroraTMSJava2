package com.auroratms.tournamententry;

/**
 * Type of membership which can be purchased
 */
public enum MembershipType {
    // obsolete
    NO_MEMBERSHIP_REQUIRED,
    TOURNAMENT_PASS_JUNIOR,
    TOURNAMENT_PASS_ADULT,
    BASIC_PLAN,
    PRO_PLAN,
    // new since 01/01/2026
    LIFETIME,
    BRONZE,
    SILVER,
    GOLD,
    TOURNAMENT_PASS
}
