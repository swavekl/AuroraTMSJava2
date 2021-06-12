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
    public Collection<TournamentEventEntity> list(@PathVariable Long tournamentId,
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
    public TournamentEventEntity list(@PathVariable Long tournamentId,
                                      @PathVariable Long eventId) {
        return tournamentEventEntityService.get(eventId);
    }

    @PostMapping("/tournament/{tournamentId}/tournamentevent")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public TournamentEventEntity create(@PathVariable Long tournamentId,
                                        @RequestBody TournamentEventEntity tournamentEventEntity) {
        return tournamentEventEntityService.create(tournamentEventEntity);
    }

    @PutMapping("/tournament/{tournamentId}/tournamentevent/{eventId}")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public TournamentEventEntity update(@PathVariable Long tournamentId,
                                        @PathVariable Long eventId,
                                        @RequestBody TournamentEventEntity tournamentEventEntity) {
        return tournamentEventEntityService.update(tournamentEventEntity);
    }

    @DeleteMapping("/tournament/{tournamentId}/tournamentevent/{tournamentEventId}")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public void delete(@PathVariable Long tournamentId,
                       @PathVariable Long tournamentEventId) {
        tournamentEventEntityService.delete(tournamentEventId);
    }
}
