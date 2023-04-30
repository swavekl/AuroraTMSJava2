package com.auroratms.match;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing match cards
 */
@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Slf4j
@Transactional
public class MatchCardController {

    @Autowired
    private MatchCardService matchCardService;

    /**
     * Gets a match card with its matches
     *
     * @param eventId
     * @return
     */
    @GetMapping("/matchcards")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<List<MatchCard>> listMatchCards(@RequestParam(required = false) Long eventId,
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
            if (matchCards != null && Boolean.TRUE.equals(includePlayerNames)) {
//                    long start = System.currentTimeMillis();
                matchCardService.fillPlayerIdToNameMapForAllMatches(matchCards);
//                    long duration = System.currentTimeMillis() - start;
//                    System.out.println("optimized duration = " + duration);
            }
            return new ResponseEntity<>(matchCards, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets a match card with its matches
     *
     * @param matchCardId
     * @return
     */
    @GetMapping("/matchcard/{matchCardId}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<MatchCard> getMatchCard(@PathVariable Long matchCardId) {
        try {
            MatchCard matchCard = matchCardService.getMatchCardWithPlayerProfiles(matchCardId);
            return new ResponseEntity<>(matchCard, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/matchcard/{matchCardId}")
    @ResponseBody
    public ResponseEntity<MatchCard> update(@RequestBody MatchCard matchCard,
                                            @PathVariable Long matchCardId) {
        try {
            log.info("updating match card: " + matchCard);
            matchCardService.save(matchCard);
            MatchCard updatedMatchCard = matchCardService.getMatchCard(matchCardId);
            return ResponseEntity.ok(updatedMatchCard);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
