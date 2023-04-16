package com.auroratms.tournamentevententry;

import com.auroratms.tournamententry.MembershipType;

import java.util.List;
import java.util.Map;

/**
 * Tournament entry and event information prior to any changes to restore after
 * user chooses to discard current cart session changes
 */
public class OriginalEntryInfo {
    String cartSessionId;
    MembershipType membershipType;
    long usattDonationAmount;
    List<Long> confirmedEvents;
    Map<Long, String> doublesEventToPartnerMap;
}
