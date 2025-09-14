package com.auroratms.match;

import com.auroratms.AbstractServiceTest;
import com.auroratms.draw.DrawType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class MatchCardServiceTest extends AbstractServiceTest {

    @Autowired
    private MatchCardService matchCardService;

    @Test
    @Disabled
    public void testRoundRobinGeneration() {
        long eventId = 65L;
        DrawType drawType = DrawType.ROUND_ROBIN;
        matchCardService.generateMatchCardsForEvent(eventId, drawType);

        int round = 0;
        MatchCard firstGroupMatchCard = matchCardService.getMatchCard(eventId, round, 1);
        assertNotNull(firstGroupMatchCard, "no match card for group 1");
        assertEquals(1, firstGroupMatchCard.getGroupNum(), "wrong group number");
        List<Match> matches = firstGroupMatchCard.getMatches();
        assertEquals(6, matches.size(), "wrong number of matches for group 1");

        for (int groupNum = 2; groupNum < 5; groupNum++) {
            firstGroupMatchCard = matchCardService.getMatchCard(eventId, round, groupNum);
            assertNotNull(firstGroupMatchCard, "no match card for group " + groupNum);
            matches = firstGroupMatchCard.getMatches();
            assertEquals(6, matches.size(), "wrong number of matches for group " + groupNum);
        }

        matchCardService.delete(eventId, drawType, 1);

        List<MatchCard> allForEvent = matchCardService.findAllForEventAndDrawType(eventId, drawType);
        assertEquals(4, allForEvent.size(), "match cards wrong number");
        for (MatchCard matchCard : allForEvent) {
            assertNotNull(matches, "matches is null");
            assertEquals(6, matches.size(), "one match per card in SE round");
            assertEquals(0, matchCard.getRound(), "wrong round of");
            for (Match match : matches) {
                assertEquals(0, match.getGame1ScoreSideA());
                assertFalse(match.isSideADefaulted(), "default A should be false");
                assertFalse(match.isSideBDefaulted(), "default B should be false");
            }
        }

        matchCardService.deleteAllForEventAndDrawType(eventId, drawType);

        allForEvent = matchCardService.findAllForEventAndDrawType(eventId, drawType);
        assertEquals(0, allForEvent.size(), "match cards exist after delete");
    }

    @Test
    public void generateSingleEliminationCards () {
        long eventId = 65L;
        DrawType drawType = DrawType.SINGLE_ELIMINATION;
        matchCardService.generateMatchCardsForEvent(eventId, drawType);

        List<MatchCard> matchCards = matchCardService.findAllForEventAndDrawType(eventId, drawType);
        assertEquals(8, matchCards.size(), "match cards wrong number");

        for (MatchCard matchCard : matchCards) {
            List<Match> matches = matchCard.getMatches();
            assertNotNull(matches, "matches is null");
            assertEquals(1, matches.size(), "one match per card in SE round");
            assertTrue((matchCard.getRound() == 4 || matchCard.getRound() == 2), "wrong round of either 4 or 2");
            for (Match match : matches) {
                assertEquals(0, match.getGame1ScoreSideA());
                assertFalse(match.isSideADefaulted(), "default should be false");
                assertFalse(match.isSideBDefaulted(), "default should be false");
            }
        }
    }
}
