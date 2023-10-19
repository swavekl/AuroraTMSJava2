package com.auroratms.tournamentevententry;

import com.auroratms.tournamententry.MembershipType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Tournament entry and event information prior to any changes to restore after
 * user chooses to discard current cart session changes
 */
@NoArgsConstructor
@Getter
@Setter
public class OriginalEntryInfo {
    private Long entryId;
    private String cartSessionId;
    private boolean withdrawing;
    private MembershipType membershipType;
    private int usattDonation;
    private Map<Long, String> doublesEventToPartnerMap;
}
