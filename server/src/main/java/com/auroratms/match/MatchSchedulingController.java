package com.auroratms.match;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
public class MatchSchedulingController {

    @Autowired
    private MatchSchedulingService matchSchedulingService;

    public MatchSchedulingController() {
    }

    @GetMapping("/schedule/{tournamentId}/{day}")
    @ResponseBody
    public ResponseEntity<MatchCard> getMatchCard(@PathVariable long tournamentId,
                                                  @PathVariable int day) {
        try {
            // gets match cards for events for this day with schedule information filled in
            List<MatchCard> daysMatchCards = matchSchedulingService.generateScheduleForDay(tournamentId, day);
            for (MatchCard matchCard : daysMatchCards) {
                matchCard.setMatches(null);
            }
            return new ResponseEntity(daysMatchCards, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/schedule")
    @ResponseBody
    public void updateMatchCards(@RequestBody List<MatchCard> matchCards) {
        matchSchedulingService.updateMatches(matchCards);
    }

}
