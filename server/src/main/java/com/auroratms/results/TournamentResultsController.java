package com.auroratms.results;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Slf4j
@Transactional
public class TournamentResultsController {

    @Autowired
    private TournamentResultsService tournamentResultsService;

    @GetMapping("/tournamentresults/{tournamentId}")
    public ResponseEntity<List<EventResultStatus>> listEventResults(@PathVariable long tournamentId) {
        try {
            List<EventResultStatus> result = tournamentResultsService.listEventResultsStatus(tournamentId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tournamentresults/{tournamentId}/event/{eventId}")
    public ResponseEntity<List<EventResults>> getEventResultDetails(@PathVariable long tournamentId,
                                                                    @PathVariable long eventId) {
        try {
            List<EventResults> result = tournamentResultsService.getEventResults(eventId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
