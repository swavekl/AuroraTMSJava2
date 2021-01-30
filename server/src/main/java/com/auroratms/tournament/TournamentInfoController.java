package com.auroratms.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class TournamentInfoController {

    @Autowired
    private TournamentService tournamentService;

    @GetMapping("/tournamentinfos")
    public Collection<TournamentInfo> list() {
        return toTournamentInfos(tournamentService.list());
    }

    @GetMapping("/tournamentinfo/{id}")
    public TournamentInfo getByKey(@PathVariable Long id) {
        return toTournamentInfo(tournamentService.getByKey(id));
    }

    private Collection<TournamentInfo> toTournamentInfos (Collection<Tournament> tournaments) {
        Collection<TournamentInfo> tournamentInfos = new ArrayList<>(tournaments.size());
        for (Tournament tournament : tournaments) {
            tournamentInfos.add(toTournamentInfo(tournament));
        }
        return tournamentInfos;
    }

    private TournamentInfo toTournamentInfo (Tournament tournament) {
        TournamentInfo tournamentInfo = new TournamentInfo();
        tournamentInfo.setId(tournament.getId());
        tournamentInfo.setName(tournament.getName());
        tournamentInfo.setCity(tournament.getCity());
        tournamentInfo.setState(tournament.getState());
        tournamentInfo.setStartDate(tournament.getStartDate());
        tournamentInfo.setEndDate(tournament.getEndDate());
        tournamentInfo.setStarLevel(tournament.getStarLevel());
        if (tournament.getConfiguration() != null) {
            tournamentInfo.setTournamentType(tournament.getConfiguration().getTournamentType());
        } else {
            tournamentInfo.setTournamentType(TournamentType.RatingsRestricted);
        }
        return tournamentInfo;
    }
}
