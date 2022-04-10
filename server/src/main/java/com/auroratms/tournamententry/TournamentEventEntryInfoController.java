package com.auroratms.tournamententry;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Service for efficient getting of tournament entries for a tournament
 */
@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
public class TournamentEventEntryInfoController {

    private final TournamentEntryInfoService tournamentEntryInfoService;

    public TournamentEventEntryInfoController(TournamentEntryInfoService tournamentEntryInfoService) {
        this.tournamentEntryInfoService = tournamentEntryInfoService;
    }

    @GetMapping("/tournamentplayers/{tournamentId}")
    public ResponseEntity getAllEntryInfosForTournament(@PathVariable Long tournamentId) {
        try {
            List<TournamentEntryInfo> entryInfos = this.tournamentEntryInfoService.getAllEntryInfosForTournament(tournamentId);
            return new ResponseEntity(entryInfos, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets event entry infos for one event
     *
     * @param eventId
     * @return
     */
    @GetMapping("/eventplayers/{eventId}")
    public ResponseEntity getDoublesEntryInfosForEvent(@PathVariable Long eventId) {
        try {
            List<TournamentEntryInfo> entryInfos = this.tournamentEntryInfoService.getDoublesEntryInfosForEvent(eventId);
            return new ResponseEntity(entryInfos, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/waitinglistentries/{tournamentId}")
    public ResponseEntity<List<TournamentEntryInfo>> getWaitingListEntries(@PathVariable Long tournamentId) {
        try {
            List<TournamentEntryInfo> waitingListEntries = this.tournamentEntryInfoService.getPlayerEntriesWithWaitingListEntries(tournamentId);
            return ResponseEntity.ok(waitingListEntries);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
