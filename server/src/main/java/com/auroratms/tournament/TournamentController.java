package com.auroratms.tournament;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class TournamentController {

    private static final Logger logger = LoggerFactory.getLogger(TournamentController.class);

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentEventHelper tournamentEventHelper;

    @GetMapping("/tournaments")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees') or hasAuthority('DataEntryClerks') or hasAuthority('Monitors') or hasAuthority('DigitalScoreBoards')")
    public Collection<Tournament> list(@RequestParam(required = false) Date today) {
        Collection<Tournament> tournaments = tournamentService.listOwned(0, 100);
        if (today != null) {
            List<Tournament> todaysTournamentList = new ArrayList<>();
            for (Tournament tournament : tournaments) {
                boolean sameAsStartDay = DateUtils.isSameDay(today, tournament.getStartDate());
                boolean sameAsEndDay = DateUtils.isSameDay(today, tournament.getEndDate());
                if (sameAsStartDay || sameAsEndDay || (today.before(tournament.getEndDate()) &&
                        today.after(tournament.getStartDate()))) {
                    todaysTournamentList.add(tournament);
                    break;
                }
            }
            return todaysTournamentList;
        } else {
            return tournaments;
        }
    }

    @GetMapping("/tournament/{id}")
    public Tournament getByKey(@PathVariable Long id) {
        return tournamentService.getByKey(id);
    }

    @PostMapping("/tournament")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public @ResponseBody Tournament save(@RequestBody Tournament tournament) {
        String name = tournament.getName();
        if (name != null && name.startsWith("--clone--")) {
            String newName = tournament.getName().substring("--clone--".length());
            tournament.setName(newName);
            long originalTournamentId = tournament.getId();
            tournament.setId(null);
            Tournament clonedTournament = tournamentService.saveTournament(tournament);
            this.tournamentEventHelper.cloneEvents(originalTournamentId, clonedTournament.getId());
            return clonedTournament;
        } else {
            return tournamentService.saveTournament(tournament);
        }
    }

    @DeleteMapping("/tournament/{id}")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            this.tournamentEventHelper.deleteEvents(id);
            this.tournamentService.deleteTournament(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting", e);
            return ResponseEntity.notFound().build();
        }
    }
}
