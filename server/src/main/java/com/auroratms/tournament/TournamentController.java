package com.auroratms.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @GetMapping("/tournaments")
    public Collection<Tournament> list() {
        return tournamentService.listOwned(0, 100);
    }

    @GetMapping("/tournament/{id}")
//    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public Tournament getByKey(@PathVariable Long id) {
        return tournamentService.getByKey(id);
    }

    @PostMapping("/tournament")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public @ResponseBody Tournament save(@RequestBody Tournament tournament) {
        return tournamentService.saveTournament(tournament);
    }
}
