package com.auroratms.match;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<MatchCard>> getMatchCard(@PathVariable long tournamentId,
                                                        @PathVariable int day) {
        try {
            // gets match cards for events for this day with schedule information filled in
            List<MatchCard> daysMatchCards = matchSchedulingService.generateScheduleForDay(tournamentId, day);
//            for (MatchCard matchCard : daysMatchCards) {
//                matchCard.setMatches(null);
//            }
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

    @PutMapping("/schedule/{tournamentId}/{day}/regenerate")
    @ResponseBody
    public ResponseEntity<List<MatchCard>> regenerateMatchCardSchedules(@PathVariable long tournamentId,
                                                                        @PathVariable int day,
                                                                        @RequestBody List<Long> matchCardIds) {
        try {
            // gets match cards for events for this day with schedule information filled in
            List<MatchCard> daysMatchCards = matchSchedulingService.generateScheduleForMatchCards(tournamentId, day, matchCardIds);
            return new ResponseEntity(daysMatchCards, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets schedule for particular tournament
     * @param tournamentId
     * @param day
     * @param tableNumber
     * @return
     */
    @GetMapping("/schedule/{tournamentId}/{day}/table/{tableNumber}")
    @ResponseBody
    public ResponseEntity<MatchCard> listMatchesForTable(@PathVariable long tournamentId,
                                                         @PathVariable int day,
                                                         @PathVariable int tableNumber) {
        try {
            List<MatchCard> matchCards = matchSchedulingService.getScheduleForTable(tournamentId, day, tableNumber);
            return new ResponseEntity(matchCards, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
