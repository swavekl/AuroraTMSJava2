package com.auroratms.tournamentevententry;

import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamententry.TournamentEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class TournamentEventEntryController {

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @GetMapping("/tournamententry/{tournamentEntryId}/tournamentevententries")
    public List<TournamentEventEntry> getAllEntries (@PathVariable Long tournamentEntryId) {
        return tournamentEventEntryService.getEntries(tournamentEntryId);
    }

    @PostMapping("/tournamententry/{tournamentEntryId}/tournamentevententry")
    public TournamentEventEntry create(@PathVariable Long tournamentEntryId,
                                        @RequestBody TournamentEventEntry tournamentEventEntry) {
        return tournamentEventEntryService.create(tournamentEventEntry);
    }

    @PutMapping("/tournamententry/{tournamentEntryId}/tournamentevententry/{eventId}")
    public TournamentEventEntry update(@PathVariable Long tournamentEntryId,
                                        @PathVariable Long eventId,
                                        @RequestBody TournamentEventEntry tournamentEventEntry) {
        return tournamentEventEntryService.update(tournamentEventEntry);
    }

    @DeleteMapping("/tournamententry/{tournamentEntryId}/tournamentevententry/{eventId}")
    public void delete(@PathVariable Long tournamentEntryId,
                       @PathVariable Long eventId) {
        tournamentEventEntryService.delete(eventId);
    }
}
