package com.auroratms.notification;

import com.auroratms.AbstractServiceTest;
import com.auroratms.tournamententry.EntryType;
import com.auroratms.tournamententry.MembershipType;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamententry.notification.TournamentEventPublisher;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Date;

public class TournamentEventPublisherTest extends AbstractServiceTest {

    @Autowired
    private TournamentEventPublisher publisher;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Test
    @WithMockUser(username = "swaveklorenc+julia@gmail.com", authorities = {"Everyone"})
    public void initiateRegistrationEmail () {

        TournamentEntry tournamentEntry = new TournamentEntry();
        tournamentEntry.setTournamentFk(153);
        tournamentEntry.setProfileId("00uwbdqm3ded2Crth0h7");
        tournamentEntry.setDateEntered(new Date());
        tournamentEntry.setEntryType(EntryType.INDIVIDUAL);
        tournamentEntry.setEligibilityRating(1234);
        tournamentEntry.setSeedRating(1255);
        tournamentEntry.setMembershipOption(MembershipType.NO_MEMBERSHIP_REQUIRED);
        TournamentEntry entry = tournamentEntryService.create(tournamentEntry);

        publisher.publishTournamentEnteredEvent(entry.getId());
    }
}
