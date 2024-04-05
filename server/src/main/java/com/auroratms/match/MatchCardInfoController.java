package com.auroratms.match;

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
            List<MatchCardInfo> matchCardInfoList = Collections.emptyList();
            if (matchCards != null) {
                // convert to Match Card infos
                matchCardInfoList = new ArrayList<>(matchCards.size());
                for (MatchCard matchCard : matchCards) {
                    MatchCardInfo matchCardInfo = new MatchCardInfo(
                            matchCard.getId(), matchCard.getDrawType(), matchCard.getRound(),
                            matchCard.getGroupNum(), matchCard.getAssignedTables(), matchCard.getStartTime());
                    matchCardInfoList.add(matchCardInfo);
                }
            }
            return new ResponseEntity<>(matchCardInfoList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
