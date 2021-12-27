package com.auroratms.match;

import com.auroratms.match.notification.MatchEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing matches
 */
@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
public class MatchController {

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchEventPublisher matchEventPublisher;

    /**
     * Gets match score
     *
     * @return Match
     */
    @GetMapping("/match/{matchId}")
    public ResponseEntity<Match> getMatch(@PathVariable long matchId) {
        try {
            Match match = matchService.getMatch(matchId);
            return new ResponseEntity<>(match, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/matches")
    public ResponseEntity<List<Match>> getAllMatches(@RequestParam long matchCardId) {
        try {
            List<Match> matches = matchService.getMatchesForCard(matchCardId);
            return new ResponseEntity<>(matches, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates match score
     *
     * @return
     */
    @PutMapping("/match/{matchId}")
    public ResponseEntity<Match> updateMatch(@RequestBody Match match,
                                             @PathVariable String matchId) {
        try {
            Match matchBeforeUpdate = matchService.getMatch(match.getId());
            Match updatedMatch = matchService.updateMatch(match);
            this.matchEventPublisher.publishMatchEvent(matchBeforeUpdate, updatedMatch);
            return new ResponseEntity<>(updatedMatch, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
