package com.auroratms.tournamentevententry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller used during registration for events to enter, drop and confirm entries
 */
@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class EventEntryStatusController {

    @Autowired
    private  EventEntryStatusService eventEntryStatusService;

    @GetMapping("/tournamententry/{tournamentEntryId}/eventstatus/list")
    public List<TournamentEventEntryInfo> list(@PathVariable Long tournamentEntryId) {
        return eventEntryStatusService.getEntriesWithStatus(tournamentEntryId);
    }

    @PostMapping("/tournamententry/{tournamentEntryId}/eventstatus/change")
    public void changeStatus(@PathVariable Long tournamentEntryId,
                        @RequestBody TournamentEventEntryInfo entryInfo) {
        eventEntryStatusService.changeStatus(tournamentEntryId, entryInfo);
    }

    @PutMapping("/tournamententry/{tournamentEntryId}/eventstatus/confirmall/{cartSessionId}")
    public void confirmAll(@PathVariable Long tournamentEntryId,
                           @PathVariable String cartSessionId,
                           @RequestParam Boolean withdrawing) {
        eventEntryStatusService.confirmAll(tournamentEntryId, cartSessionId, withdrawing);
    }
}
