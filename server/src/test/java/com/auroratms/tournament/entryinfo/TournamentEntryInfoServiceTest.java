package com.auroratms.tournament.entryinfo;

import com.auroratms.AbstractServiceTest;
import com.auroratms.tournamententry.TournamentEntryInfo;
import com.auroratms.tournamententry.TournamentEntryInfoService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@Transactional
public class TournamentEntryInfoServiceTest extends AbstractServiceTest {

    @Autowired
    private TournamentEntryInfoService tournamentEntryInfoService;

    @Test
    public void testAll () {
        Date start = new Date();
        List<TournamentEntryInfo> allEntryInfosForTournament = tournamentEntryInfoService.getAllEntryInfosForTournament(133);
        Date end = new Date();
        long diff = end.getTime() - start.getTime();
        System.out.println("query took " + diff + " ms");
        assertTrue("", allEntryInfosForTournament.size() > 0);

        for (TournamentEntryInfo tournamentEntryInfo : allEntryInfosForTournament) {
            if (tournamentEntryInfo.getFirstName() == null || tournamentEntryInfo.getLastName() == null) {
                System.out.println("incomplete tournamentEntryInfo = " + tournamentEntryInfo);
            }
            assertNotNull("first name is null for player profile " + tournamentEntryInfo.getProfileId(), tournamentEntryInfo.getFirstName());
            assertNotNull("last name is null for player profile " + tournamentEntryInfo.getProfileId(), tournamentEntryInfo.getLastName());
            assertNotNull("events are empty for player profile " + tournamentEntryInfo.getProfileId(), tournamentEntryInfo.getEventIds());
        }
    }
}
