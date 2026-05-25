package com.auroratms.match;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller for retrieving minimal match card information
 */
@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Slf4j
@Transactional
public class MatchCardInfoController {

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @GetMapping("/matchcardinfos")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<List<MatchCardInfo>> listMatchCardInfos(@RequestParam(required = false) Long eventId,
                                                                  @RequestParam(required = false) Long tournamentId,
                                                                  @RequestParam(required = false) Integer day,
                                                                  @RequestParam(required = false) Boolean includePlayerNames) {
        try {
            List<MatchCard> matchCards = null;
            if (eventId != null) {
                matchCards = matchCardService.findAllForEvent(eventId);
            } else if (tournamentId != null && day != null) {
                matchCards = matchCardService.findAllForTournamentAndDay(tournamentId, day);
            }
            TournamentEvent tournamentEvent = tournamentEventEntityService.get(eventId);
            List<MatchCardInfo> matchCardInfoList = Collections.emptyList();
            if (matchCards != null) {
                // convert to Match Card infos
                matchCardInfoList = new ArrayList<>(matchCards.size());
                for (MatchCard matchCard : matchCards) {
                    List<Match> matches = matchCard.getMatches();
                    List<String> matchesResults = new ArrayList<>(matches.size());
                    for (Match match : matches) {
                        // get match result if available
                        if (match.isMatchDoubleDefaulted() || match.isMatchFinished(match.getNumberOfGames(), match.getPointsPerGame())) {
                            if (!match.isMatchDoubleDefaulted()) {
                                Character winnerLetter = (match.isMatchWinner(match.getPlayerAProfileId(), match.getNumberOfGames(), match.getPointsPerGame()))
                                        ? match.getPlayerALetter() : match.getPlayerBLetter();
                                String compactResult = match.getCompactResult(match.getNumberOfGames(), match.getPointsPerGame());
                                String matchResult = winnerLetter + " => " + compactResult;
                                matchesResults.add(matchResult);
                            }
                        }
                    }

                    MatchCardInfo matchCardInfo = new MatchCardInfo(
                            matchCard.getId(), matchCard.getDrawType(), matchCard.getRound(),
                            matchCard.getGroupNum(), matchCard.getAssignedTables(), matchCard.getStartTime(),
                            matchesResults, matchCard.getPlayerRankingsAsMap(), matchCard.getAdvancingPlayerIdsAsList());
                    matchCardInfoList.add(matchCardInfo);
                }
            }
            return new ResponseEntity<>(matchCardInfoList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
