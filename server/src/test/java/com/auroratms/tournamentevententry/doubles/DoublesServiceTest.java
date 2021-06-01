package com.auroratms.tournamentevententry.doubles;

import com.auroratms.AbstractServiceTest;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

@Transactional
public class DoublesServiceTest extends AbstractServiceTest {

    @Autowired
    private DoublesService doublesService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @Autowired
    private UserProfileService userProfileService;

    @Test
    @WithMockUser(username = "swaveklorenc@yahoo.com", authorities = {"Admins"})
    public void testMakingPair () {

        // find doubles event
        long tournamentFk = 153L;
        long doublesEventId = 0L;
        Collection<TournamentEventEntity> events = tournamentEventEntityService.list(tournamentFk, Pageable.unpaged());
        for (TournamentEventEntity event : events) {
            if (event.isDoubles() && event.getName().equals("Open Doubles")) {
                doublesEventId = event.getId();
                break;
            }
        }
        assertTrue("", doublesEventId != 0);

        Collection<UserProfile> allPlayersProfiles = userProfileService.list(null, "Alguetti");
//        Collection<UserProfile> playerProfiles = userProfileService.list("Gal", "Alguetti");
//        assertTrue("", playerProfiles.size() > 0);
//        allPlayersProfiles.addAll(playerProfiles);
//        playerProfiles = userProfileService.list("Sharon", "Alguetti");
//        assertTrue("", playerProfiles.size() > 0);
//        allPlayersProfiles.addAll(playerProfiles);
        String sharonProfileId = null;
        String galProfileId = null;
        for (UserProfile userProfile : allPlayersProfiles) {
            if (userProfile.getFirstName().equals("Gal")) {
                galProfileId = userProfile.getUserId();
            } else if (userProfile.getFirstName().equals("Sharon")) {
                sharonProfileId = userProfile.getUserId();
            }
        }
        assertNotNull(sharonProfileId);
        assertNotNull(galProfileId);

        long sharonsEventEntryId = 0;
        // set profile ids for player A and B
        List<TournamentEntry> sharonsTournamentEntries = tournamentEntryService.listForTournamentAndUser(tournamentFk, sharonProfileId);
        assertEquals("", 1, sharonsTournamentEntries.size());
        long sharonsTournamentEntryId = sharonsTournamentEntries.get(0).getId();
        int sharonsEligibilityRating = sharonsTournamentEntries.get(0).getEligibilityRating();
        int sharonsSeedRating = sharonsTournamentEntries.get(0).getSeedRating();
        List<TournamentEventEntry> sharonsEventEntries = tournamentEventEntryService.getEntries(sharonsTournamentEntryId);
        for (TournamentEventEntry playerEventEntry : sharonsEventEntries) {
            // if player entered a doubles event
            if (doublesEventId == playerEventEntry.getTournamentEventFk()) {
                playerEventEntry.setDoublesPartnerProfileId(galProfileId);
                sharonsEventEntryId = playerEventEntry.getId();
                break;
            }
        }

        long galsEventEntryId = 0;
        List<TournamentEntry> galsTournamentEntries = tournamentEntryService.listForTournamentAndUser(tournamentFk, galProfileId);
        assertEquals("", 1, galsTournamentEntries.size());
        long galsTournamentEntryId = galsTournamentEntries.get(0).getId();
        int galsEligibilityRating = galsTournamentEntries.get(0).getEligibilityRating();
        int galsSeedRating = galsTournamentEntries.get(0).getSeedRating();
        List<TournamentEventEntry> galsEventEntries = tournamentEventEntryService.getEntries(galsTournamentEntryId);
        for (TournamentEventEntry playerEventEntry : galsEventEntries) {
            // if player entered a doubles event
            if (doublesEventId == playerEventEntry.getTournamentEventFk()) {
                playerEventEntry.setDoublesPartnerProfileId(sharonProfileId);
                galsEventEntryId = playerEventEntry.getId();
                break;
            }
        }

        List<DoublesPair> doublesPairList = doublesService.makeRequestedPairs(sharonsTournamentEntryId);
        assertTrue("", doublesPairList.size() == 1);
        assertNotNull(doublesPairList);
        DoublesPair doublesPair = doublesPairList.get(0);

        long playerAEventEntryFk = doublesPair.getPlayerAEventEntryFk();
        assertEquals("wrong player A event entry Id", sharonsEventEntryId, playerAEventEntryFk);

        long playerBEventEntryFk = doublesPair.getPlayerBEventEntryFk();
        assertEquals("wrong player B event entry Id", galsEventEntryId, playerBEventEntryFk);

        int pairEligibilityRating = doublesPair.getEligibilityRating();
        assertEquals("", sharonsEligibilityRating + galsEligibilityRating, pairEligibilityRating);

        int pairSeedRating = doublesPair.getSeedRating();
        assertEquals("", sharonsSeedRating + galsSeedRating, pairSeedRating);

        List<DoublesPair> playerDoublesEntry = doublesService.findPlayerDoublesEntry(doublesEventId, sharonsEventEntryId);
        assertEquals("no entry after making", 1, playerDoublesEntry.size());

        // change the player
        Collection<UserProfile> playerProfiles = userProfileService.list("Adar", "Alguetti");
        assertTrue("", playerProfiles.size() > 0);
        String adarsProfileId = null;
        for (UserProfile userProfile : playerProfiles) {
            if (userProfile.getFirstName().equals("Adar")) {
                adarsProfileId = userProfile.getUserId();
            }
        }
        assertNotNull (adarsProfileId);

        List<TournamentEntry> adarsTournamentEntries = tournamentEntryService.listForTournamentAndUser(tournamentFk, adarsProfileId);
        assertEquals("", 1, adarsTournamentEntries.size());
        long adarsTournamentEntryId = adarsTournamentEntries.get(0).getId();
        int adarsEligibilityRating = adarsTournamentEntries.get(0).getEligibilityRating();
        int adarsSeedRating = adarsTournamentEntries.get(0).getSeedRating();
        long adarsEventEntryId = 0L;
        List<TournamentEventEntry> adarsEventEntries = tournamentEventEntryService.getEntries(adarsTournamentEntryId);
        for (TournamentEventEntry playerEventEntry : adarsEventEntries) {
            // if player entered a doubles event
            if (doublesEventId == playerEventEntry.getTournamentEventFk()) {
                playerEventEntry.setDoublesPartnerProfileId(sharonProfileId);
                adarsEventEntryId = playerEventEntry.getId();
                break;
            }
        }

        // Change Sharon's partner from Gal to Adar
        sharonsEventEntries = tournamentEventEntryService.getEntries(sharonsTournamentEntryId);
        for (TournamentEventEntry playerEventEntry : sharonsEventEntries) {
            // if player entered a doubles event
            if (doublesEventId == playerEventEntry.getTournamentEventFk()) {
                playerEventEntry.setDoublesPartnerProfileId(adarsProfileId);
                break;
            }
        }

        doublesPairList = doublesService.makeRequestedPairs(sharonsTournamentEntryId);
        assertTrue("", doublesPairList.size() == 1);
        assertNotNull(doublesPairList);
        doublesPair = doublesPairList.get(0);

        playerAEventEntryFk = doublesPair.getPlayerAEventEntryFk();
        assertEquals("wrong player A event entry Id", sharonsEventEntryId, playerAEventEntryFk);

        playerBEventEntryFk = doublesPair.getPlayerBEventEntryFk();
        assertEquals("wrong player B event entry Id", adarsEventEntryId, playerBEventEntryFk);

        pairEligibilityRating = doublesPair.getEligibilityRating();
        assertEquals("", sharonsEligibilityRating + adarsEligibilityRating, pairEligibilityRating);

        pairSeedRating = doublesPair.getSeedRating();
        assertEquals("", sharonsSeedRating + adarsSeedRating, pairSeedRating);

        // break up a pair
        doublesService.deletePair(doublesPair.getId());

        playerDoublesEntry = doublesService.findPlayerDoublesEntry(doublesEventId, sharonsEventEntryId);
        assertEquals("there should be no entries after break up", 0, playerDoublesEntry.size());
    }
}
