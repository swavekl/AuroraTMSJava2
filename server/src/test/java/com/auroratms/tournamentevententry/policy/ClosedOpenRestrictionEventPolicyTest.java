package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEvent;
import com.auroratms.tournament.EligibilityRestriction;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClosedOpenRestrictionEventPolicyTest {

    @Test
    public void testStateClosed() {
        ClosedOpenRestrictionEventPolicy policy = new ClosedOpenRestrictionEventPolicy(
                "IL", "PA", "US", "US");
        TournamentEvent event = new TournamentEvent();
        event.setEligibilityRestriction(EligibilityRestriction.CLOSED_STATE);
        boolean entryDenied = policy.isEntryDenied(event);
        assertTrue("entryDenied should be true", entryDenied);
    }

    @Test
    public void testStateClosedOK() {
        ClosedOpenRestrictionEventPolicy policy = new ClosedOpenRestrictionEventPolicy(
                "IL", "IL", "US", "US");
        TournamentEvent event = new TournamentEvent();
        event.setEligibilityRestriction(EligibilityRestriction.CLOSED_STATE);
        boolean entryDenied = policy.isEntryDenied(event);
        assertFalse("entryDenied should be false", entryDenied);
    }

    @Test
    public void testRegionalClosed() {
        ClosedOpenRestrictionEventPolicy policy = new ClosedOpenRestrictionEventPolicy(
                "IL", "PA", "US", "US");
        TournamentEvent event = new TournamentEvent();
        event.setEligibilityRestriction(EligibilityRestriction.CLOSED_REGIONAL);
        boolean entryDenied = policy.isEntryDenied(event);
        assertTrue("entryDenied should be true", entryDenied);

        ClosedOpenRestrictionEventPolicy policy2 = new ClosedOpenRestrictionEventPolicy(
                "IL", "IN", "US", "US");
        boolean entryDenied2 = policy2.isEntryDenied(event);
        assertFalse("entryDenied should be false", entryDenied2);
    }

    @Test
    public void testRegionalClosedOK() {
        ClosedOpenRestrictionEventPolicy policy = new ClosedOpenRestrictionEventPolicy(
                "IL", "IL", "US", "US");
        TournamentEvent event = new TournamentEvent();
        event.setEligibilityRestriction(EligibilityRestriction.CLOSED_REGIONAL);
        boolean entryDenied = policy.isEntryDenied(event);
        assertFalse("entryDenied should be false", entryDenied);
    }

    @Test
    public void testNationalClosed() {
        ClosedOpenRestrictionEventPolicy policy = new ClosedOpenRestrictionEventPolicy(
                "IL", "PA", "US", "US");
        TournamentEvent event = new TournamentEvent();
        event.setEligibilityRestriction(EligibilityRestriction.CLOSED_NATIONAL);
        boolean entryDenied = policy.isEntryDenied(event);
        assertFalse("entryDenied should be false", entryDenied);

        ClosedOpenRestrictionEventPolicy policy2 = new ClosedOpenRestrictionEventPolicy(
                "IL", "IN", "US", "US");
        boolean entryDenied2 = policy2.isEntryDenied(event);
        assertFalse("entryDenied should be false", entryDenied2);
    }

    @Test
    public void testNationalClosedOK() {
        ClosedOpenRestrictionEventPolicy policy = new ClosedOpenRestrictionEventPolicy(
                "IL", "ON", "US", "CA");
        TournamentEvent event = new TournamentEvent();
        event.setEligibilityRestriction(EligibilityRestriction.CLOSED_NATIONAL);
        boolean entryDenied = policy.isEntryDenied(event);
        assertTrue("entryDenied should be true", entryDenied);
    }
}
