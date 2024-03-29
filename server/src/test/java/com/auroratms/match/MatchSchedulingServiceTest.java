package com.auroratms.match;

import com.auroratms.AbstractServiceTest;
import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

public class MatchSchedulingServiceTest extends AbstractServiceTest {

    @Autowired
    private MatchSchedulingService matchSchedulingService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;


    @Test
    public void testThreeDayTournamentScheduleGeneration() {
        long previousEventFk = 0L;
        TournamentEvent tournamentEvent = null;
        int numTablesPerGroup = 2;
        for (int day = 1; day <= 3; day++) {
            List<MatchCard> matchCards = matchSchedulingService.generateScheduleForDay(153L, day);
            for (MatchCard matchCard : matchCards) {
                long eventFk = matchCard.getEventFk();
                if (eventFk != previousEventFk) {
                    tournamentEvent = tournamentEventEntityService.get(eventFk);
                    numTablesPerGroup = tournamentEvent.getNumTablesPerGroup();
                    System.out.println("event = " + tournamentEvent.getName() + " numTablesPerGroup = " + numTablesPerGroup);
                    previousEventFk = eventFk;
                }
            }
        }
    }

    private void checkMatchCard(TournamentEvent tournamentEvent, int numTablesPerGroup, MatchCard matchCard, long eventFk) {
        String assignedTables = matchCard.getAssignedTables();
        int groupNum = matchCard.getGroupNum();
        assertNotEquals("tables are not assigned for match for group " + groupNum + " in event " + tournamentEvent.getName(),
                "", assignedTables);
        String[] tableNumbers = assignedTables.split(",");
        int numMatches = matchCard.getMatches().size();
        System.out.println("groupNum " + groupNum + " numMatches " + numMatches);
        if (matchCard.getDrawType() == DrawType.ROUND_ROBIN) {
            int expectedAssignedTablesCount = (numMatches == 6) ? ((numTablesPerGroup == 2) ? 2 : 1) : 1;
            assertEquals("wrong number of tables for group " + groupNum + " in event " + tournamentEvent.getName(),
                    expectedAssignedTablesCount, tableNumbers.length);
            int expectedDuration = (numMatches == 6) ? ((numTablesPerGroup == 2) ? 90 : 180) : 90;
            assertEquals("wrong matches duration for group " + groupNum,
                    expectedDuration, matchCard.getDuration());
        } else {
            assertEquals("wrong number of tables for group " + groupNum,
                    1, tableNumbers.length);
            int numberOfGames = matchCard.getNumberOfGames();
            int expectedDuration = (numberOfGames == 3) ? 20 : ((numberOfGames == 5) ? 30 : 60);
            assertEquals("wrong matches duration for group " + groupNum,
                    expectedDuration, matchCard.getDuration());
        }
    }
}
