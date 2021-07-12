package com.auroratms.match;

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
    public ResponseEntity<List<MatchCard>> listMatchCards(@RequestParam long eventId) {
        try {
            List<MatchCard> matchCards = matchCardService.findAllForEvent(eventId);
//            for (MatchCard matchCard : matchCards) {
//                matchCard.setMatches(null);
//            }
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
            MatchCard matchCard = matchCardService.get(matchCardId);
//            List<Match> matches = matchCard.getMatches();
//            for (Match match : matches) {
//                match.setMatchCard(null);
//            }
            return new ResponseEntity<>(matchCard, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
