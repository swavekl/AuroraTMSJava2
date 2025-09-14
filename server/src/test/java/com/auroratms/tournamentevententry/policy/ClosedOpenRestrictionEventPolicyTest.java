package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEvent;
import com.auroratms.tournament.EligibilityRestriction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClosedOpenRestrictionEventPolicyTest {

    @Test
    public void testStateClosed() {
        ClosedOpenRestrictionEventPolicy policy = new ClosedOpenRestrictionEventPolicy(
                "IL", "PA", "US", "US");
        TournamentEvent event = new TournamentEvent();
        event.setEligibilityRestriction(EligibilityRestriction.CLOSED_STATE);
        boolean entryDenied = policy.isEntryDenied(event);
        assertTrue(entryDenied, "entryDenied should be true");
    }

    @Test
    public void testStateClosedOK() {
        ClosedOpenRestrictionEventPolicy policy = new ClosedOpenRestrictionEventPolicy(
                "IL", "IL", "US", "US");
        TournamentEvent event = new TournamentEvent();
        event.setEligibilityRestriction(EligibilityRestriction.CLOSED_STATE);
        boolean entryDenied = policy.isEntryDenied(event);
        assertFalse(entryDenied, "entryDenied should be false");
    }

    @Test
    public void testRegionalClosed() {
        ClosedOpenRestrictionEventPolicy policy = new ClosedOpenRestrictionEventPolicy(
                "IL", "PA", "US", "US");
        TournamentEvent event = new TournamentEvent();
        event.setEligibilityRestriction(EligibilityRestriction.CLOSED_REGIONAL);
        boolean entryDenied = policy.isEntryDenied(event);
        assertTrue(entryDenied, "entryDenied should be true");

        ClosedOpenRestrictionEventPolicy policy2 = new ClosedOpenRestrictionEventPolicy(
                "IL", "IN", "US", "US");
        boolean entryDenied2 = policy2.isEntryDenied(event);
        assertFalse(entryDenied2, "entryDenied should be false");
    }

    @Test
    public void testRegionalClosedOK() {
        ClosedOpenRestrictionEventPolicy policy = new ClosedOpenRestrictionEventPolicy(
                "IL", "IL", "US", "US");
        TournamentEvent event = new TournamentEvent();
        event.setEligibilityRestriction(EligibilityRestriction.CLOSED_REGIONAL);
        boolean entryDenied = policy.isEntryDenied(event);
        assertFalse(entryDenied, "entryDenied should be false");
    }

    @Test
    public void testNationalClosed() {
        ClosedOpenRestrictionEventPolicy policy = new ClosedOpenRestrictionEventPolicy(
                "IL", "PA", "US", "US");
        TournamentEvent event = new TournamentEvent();
        event.setEligibilityRestriction(EligibilityRestriction.CLOSED_NATIONAL);
        boolean entryDenied = policy.isEntryDenied(event);
        assertFalse(entryDenied, "entryDenied should be false");

        ClosedOpenRestrictionEventPolicy policy2 = new ClosedOpenRestrictionEventPolicy(
                "IL", "IN", "US", "US");
        boolean entryDenied2 = policy2.isEntryDenied(event);
        assertFalse(entryDenied2, "entryDenied should be false");
    }

    @Test
    public void testNationalClosedOK() {
        ClosedOpenRestrictionEventPolicy policy = new ClosedOpenRestrictionEventPolicy(
                "IL", "ON", "US", "CA");
        TournamentEvent event = new TournamentEvent();
        event.setEligibilityRestriction(EligibilityRestriction.CLOSED_NATIONAL);
        boolean entryDenied = policy.isEntryDenied(event);
        assertTrue(entryDenied, "entryDenied should be true");
    }
}
