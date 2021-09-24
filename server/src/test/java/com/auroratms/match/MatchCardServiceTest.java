package com.auroratms.match;

import com.auroratms.AbstractServiceTest;
import com.auroratms.draw.DrawType;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@Transactional
public class MatchCardServiceTest extends AbstractServiceTest {

    @Autowired
    private MatchCardService matchCardService;

    @Test
    @Ignore
    public void testRoundRobinGeneration() {
        long eventId = 65L;
        DrawType drawType = DrawType.ROUND_ROBIN;
        matchCardService.generateMatchCardsForEvent(eventId, drawType);

        int round = 0;
        MatchCard firstGroupMatchCard = matchCardService.getMatchCard(eventId, round, 1);
        assertNotNull("no match card for group 1", firstGroupMatchCard);
        assertEquals("wrong group number", 1, firstGroupMatchCard.getGroupNum());
        List<Match> matches = firstGroupMatchCard.getMatches();
        assertEquals("wrong number of matches for group 1", 6, matches.size());

        for (int groupNum = 2; groupNum < 5; groupNum++) {
            firstGroupMatchCard = matchCardService.getMatchCard(eventId, round, groupNum);
            assertNotNull("no match card for group " + groupNum, firstGroupMatchCard);
            matches = firstGroupMatchCard.getMatches();
            assertEquals("wrong number of matches for group " + groupNum, 6, matches.size());
        }

        matchCardService.delete(eventId, drawType, 1);

        List<MatchCard> allForEvent = matchCardService.findAllForEventAndDrawType(eventId, drawType);
        assertEquals("match cards wrong number", 4, allForEvent.size());
        for (MatchCard matchCard : allForEvent) {
            assertNotNull("matches is null", matches);
            assertEquals("one match per card in SE round", 6, matches.size());
            assertEquals("wrong round of", 0, matchCard.getRound());
            for (Match match : matches) {
                assertEquals(0, match.getGame1ScoreSideA());
                assertFalse("default A should be false", match.isSideADefaulted());
                assertFalse("default B should be false", match.isSideBDefaulted());
            }
        }

        matchCardService.deleteAllForEventAndDrawType(eventId, drawType);

        allForEvent = matchCardService.findAllForEventAndDrawType(eventId, drawType);
        assertEquals("match cards exist after delete", 0, allForEvent.size());
    }

    @Test
    public void generateSingleEliminationCards () {
        long eventId = 65L;
        DrawType drawType = DrawType.SINGLE_ELIMINATION;
        matchCardService.generateMatchCardsForEvent(eventId, drawType);

        List<MatchCard> matchCards = matchCardService.findAllForEventAndDrawType(eventId, drawType);
        assertEquals("match cards wrong number", 8, matchCards.size());

        for (MatchCard matchCard : matchCards) {
            List<Match> matches = matchCard.getMatches();
            assertNotNull("matches is null", matches);
            assertEquals("one match per card in SE round", 1, matches.size());
            assertTrue("wrong round of either 4 or 2", (matchCard.getRound() == 4 || matchCard.getRound() == 2));
            for (Match match : matches) {
                assertEquals(0, match.getGame1ScoreSideA());
                assertFalse("default should be false", match.isSideADefaulted());
                assertFalse("default should be false", match.isSideBDefaulted());
            }
        }
    }
}
