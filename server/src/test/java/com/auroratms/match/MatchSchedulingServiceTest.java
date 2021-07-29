package com.auroratms.match;

import com.auroratms.AbstractServiceTest;
import com.auroratms.draw.DrawType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

public class MatchSchedulingServiceTest extends AbstractServiceTest {

    @Autowired
    private MatchSchedulingService matchSchedulingService;

    @Autowired
    private MatchCardService matchCardService;

    @Test
    public void testDayOneScheduleGeneration() {
        List<MatchCard> addDay1MatchCards = matchSchedulingService.generateScheduleForDay(153L, 1);
        for (MatchCard matchCard : addDay1MatchCards) {
            if (matchCard.getEventFk() == 46L) {
                String assignedTables = matchCard.getAssignedTables();
                assertNotEquals("tables are not assigned for match for group " + matchCard.getGroupNum(), "", assignedTables);
                String[] tableNumbers = assignedTables.split(",");
                int expectedAssignedTablesCount = (matchCard.getMatches().size() == 6) ? 2 : 1;
                assertEquals("wrong number of tables for group " + matchCard.getGroupNum(),
                        expectedAssignedTablesCount, tableNumbers.length);
                assertEquals("wrong matches duration for group " + matchCard.getGroupNum(),
                        90, matchCard.getDuration());
            }
        }
    }
}
