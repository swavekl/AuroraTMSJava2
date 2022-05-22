package com.auroratms.tournament;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentEventCloner tournamentEventCloner;

    @GetMapping("/tournaments")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees') or hasAuthority('DataEntryClerks') or hasAuthority('Monitors') or hasAuthority('DigitalScoreBoards')")
    public Collection<Tournament> list(@RequestParam (required = false) Date today) {
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
            this.tournamentEventCloner.cloneEvents(originalTournamentId, clonedTournament.getId());
            return clonedTournament;
        } else {
            return tournamentService.saveTournament(tournament);
        }
    }
}
