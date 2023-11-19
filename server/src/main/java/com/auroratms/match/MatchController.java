package com.auroratms.match;

import com.auroratms.match.notification.MatchEventPublisher;
import com.auroratms.profile.UserProfileService;
import com.auroratms.users.UserRolesHelper;
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

    @Autowired
    private UserProfileService userProfileService;

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
            String currentUserName = UserRolesHelper.getCurrentUsername();
            String profileByLoginId = userProfileService.getProfileByLoginId(currentUserName);
            Match oldMatch = matchService.getMatch(match.getId());
            Match matchBeforeUpdate = oldMatch.clone();
            // if date entry clerk did not clear this match score then record who entered it
            // i.e. either player via phone or data entry clerk on desktop/laptop
            if (!(matchBeforeUpdate.getScoreEnteredByProfileId() != null && match.getScoreEnteredByProfileId() == null)) {
                // record user who made the change
                match.setScoreEnteredByProfileId(profileByLoginId);
            }
            Match updatedMatch = matchService.updateMatch(match);
            this.matchEventPublisher.publishMatchEvent(matchBeforeUpdate, updatedMatch, profileByLoginId);
            return new ResponseEntity<>(updatedMatch, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Locks the match allowing only one player to enter the score
     * @param matchId
     * @param profileId
     * @return
     */
    @PutMapping("/match/lock/{matchId}/{profileId}")
    public ResponseEntity<Boolean> lockMatch(@PathVariable Long matchId,
                                             @PathVariable String profileId) {
        try {
            boolean success = matchService.lockMatch(matchId, profileId);
            return new ResponseEntity<>(success, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
