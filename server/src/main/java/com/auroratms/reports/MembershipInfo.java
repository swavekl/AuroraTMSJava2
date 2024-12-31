package com.auroratms.reports;

import com.auroratms.tournamententry.MembershipType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Minimal membership information used to verify if any player didn't pay for membership
 */
@Data
@NoArgsConstructor
public class MembershipInfo {

    private String playerName;

    private String profileId;

    private Date expirationDate;

    private Long membershipId;

    private MembershipType membershipType;

    private Long entryId;
}
