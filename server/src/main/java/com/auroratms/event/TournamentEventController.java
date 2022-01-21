package com.auroratms.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class TournamentEventController {

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @GetMapping("/tournament/{tournamentId}/tournamentevents")
//    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public Collection<TournamentEvent> list(@PathVariable Long tournamentId,
                                            Pageable pageable,
                                            @RequestParam(required = false) Boolean doublesOnly) {
        if (Boolean.TRUE.equals(doublesOnly)) {
            return tournamentEventEntityService.listDoublesEvents(tournamentId);
        } else {
            return tournamentEventEntityService.list(tournamentId, pageable);
        }
    }

    @GetMapping("/tournament/{tournamentId}/tournamentevent/{eventId}")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public TournamentEvent list(@PathVariable Long tournamentId,
                                @PathVariable Long eventId) {
        return tournamentEventEntityService.get(eventId);
    }

    @PostMapping("/tournament/{tournamentId}/tournamentevent")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public TournamentEvent create(@PathVariable Long tournamentId,
                                  @RequestBody TournamentEvent tournamentEvent) {
        return tournamentEventEntityService.create(tournamentEvent);
    }

    @PutMapping("/tournament/{tournamentId}/tournamentevent/{eventId}")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public TournamentEvent update(@PathVariable Long tournamentId,
                                  @PathVariable Long eventId,
                                  @RequestBody TournamentEvent tournamentEvent) {
        return tournamentEventEntityService.update(tournamentEvent);
    }

    @DeleteMapping("/tournament/{tournamentId}/tournamentevent/{tournamentEventId}")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public void delete(@PathVariable Long tournamentId,
                       @PathVariable Long tournamentEventId) {
        tournamentEventEntityService.delete(tournamentEventId);
    }
}
