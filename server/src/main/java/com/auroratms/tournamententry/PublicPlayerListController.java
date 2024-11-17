package com.auroratms.tournamententry;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentInfo;
import com.auroratms.tournament.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

/**
 * Controller which allows to get data used on player list screen without being logged in
 */
@RestController
@RequestMapping("publicapi")
@Transactional
public class PublicPlayerListController {

    @Autowired
    private TournamentEntryInfoService tournamentEntryInfoService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private TournamentService tournamentService;

    @GetMapping("/tournamentplayers/{tournamentId}")
    public ResponseEntity<List<TournamentEntryInfo>> getAllEntryInfosForTournament(@PathVariable Long tournamentId) {
        try {
            List<TournamentEntryInfo> entryInfos = this.tournamentEntryInfoService.getAllEntryInfosForTournament(tournamentId);
            return ResponseEntity.ok(entryInfos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tournament/{tournamentId}/tournamentevents")
    public Collection<TournamentEvent> list(@PathVariable Long tournamentId,
                                            Pageable pageable,
                                            @RequestParam(required = false) Boolean doublesOnly) {
        if (Boolean.TRUE.equals(doublesOnly)) {
            return tournamentEventEntityService.listDoublesEvents(tournamentId);
        } else {
            return tournamentEventEntityService.list(tournamentId, pageable);
        }
    }

    @GetMapping("/tournamentinfo/{id}")
    public TournamentInfo getByKey(@PathVariable Long id) {
        return toTournamentInfo(tournamentService.getByKey(id));
    }

    private TournamentInfo toTournamentInfo(Tournament tournament) {
        TournamentInfo tournamentInfo = new TournamentInfo();
        tournamentInfo.setId(tournament.getId());
        tournamentInfo.setName(tournament.getName());
        tournamentInfo.setVenueName(tournament.getVenueName());
        tournamentInfo.setStreetAddress(tournament.getStreetAddress());
        tournamentInfo.setCity(tournament.getCity());
        tournamentInfo.setState(tournament.getState());
        tournamentInfo.setStartDate(tournament.getStartDate());
        tournamentInfo.setEndDate(tournament.getEndDate());
        tournamentInfo.setStarLevel(tournament.getStarLevel());
        return tournamentInfo;
    }
}
