package com.auroratms.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @GetMapping("/tournaments")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees') or hasAuthority('DataEntryClerks') or hasAuthority('Monitors') ")
    public Collection<Tournament> list() {
        return tournamentService.listOwned(0, 100);
    }

    @GetMapping("/tournament/{id}")
    public Tournament getByKey(@PathVariable Long id) {
        return tournamentService.getByKey(id);
    }

    @PostMapping("/tournament")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public @ResponseBody Tournament save(@RequestBody Tournament tournament) {
        return tournamentService.saveTournament(tournament);
    }
}
