package com.auroratms.tournamententry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class TournamentEntryController {

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @PostMapping("/tournamententry")
//    @PreAuthorize("hasAuthority('TournamentDirector') or hasAuthority('Admins')")
    public TournamentEntry create(@RequestBody TournamentEntry tournamentEntry) {
        return tournamentEntryService.create(tournamentEntry);
    }

    @GetMapping("/tournamententry/{entryId}")
//    @PreAuthorize("hasAuthority('TournamentDirector') or hasAuthority('Admins')")
    public TournamentEntry get(@PathVariable Long entryId) {
        return tournamentEntryService.get(entryId);
    }

    @PutMapping("/tournamententry/{entryId}")
//    @PreAuthorize("hasAuthority('TournamentDirector') or hasAuthority('Admins')")
    public TournamentEntry update(@PathVariable Long entryId,
                                  @RequestBody TournamentEntry tournamentEntry) {
        return tournamentEntryService.update(tournamentEntry);
    }

    @DeleteMapping("/tournamententry/{entryId}")
    public void delete(@PathVariable Long entryId) {
        tournamentEntryService.delete(entryId);
    }
}
