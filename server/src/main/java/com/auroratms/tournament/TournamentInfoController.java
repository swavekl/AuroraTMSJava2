package com.auroratms.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class TournamentInfoController {

    @Autowired
    private TournamentService tournamentService;

    @GetMapping("/tournamentinfos")
    public Collection<TournamentInfo> list(@RequestParam(required = false) Date date) {
        if (date != null) {
            return toTournamentInfos(tournamentService.listTournamentsAfterDate(date));
        } else {
            return toTournamentInfos(tournamentService.list());
        }
    }

    @GetMapping("/tournamentinfo/{id}")
    public TournamentInfo getByKey(@PathVariable Long id) {
        return toTournamentInfo(tournamentService.getByKey(id));
    }

    private Collection<TournamentInfo> toTournamentInfos (Collection<Tournament> tournaments) {
        Collection<TournamentInfo> tournamentInfos = new ArrayList<>(tournaments.size());
        for (Tournament tournament : tournaments) {
            if (tournament.isReady()) {
                tournamentInfos.add(toTournamentInfo(tournament));
            }
        }
        return tournamentInfos;
    }

    private TournamentInfo toTournamentInfo (Tournament tournament) {
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
        tournamentInfo.setTournamentDirectorName(tournament.getContactName());
        tournamentInfo.setTournamentDirectorEmail(tournament.getEmail());
        tournamentInfo.setTournamentDirectorPhone(tournament.getPhone());
        if (tournament.getConfiguration() != null) {
            tournamentInfo.setTournamentType(tournament.getConfiguration().getTournamentType());
            tournamentInfo.setCheckInType(tournament.getConfiguration().getCheckInType());
        } else {
            tournamentInfo.setCheckInType(CheckInType.DAILY);
            tournamentInfo.setTournamentType(TournamentType.RatingsRestricted);
        }
        tournamentInfo.setNumEntries(tournament.getNumEntries());
        tournamentInfo.setMaxNumEventEntries(tournament.getMaxNumEventEntries());
        tournamentInfo.setNumEventEntries(tournament.getNumEventEntries());
        if (tournamentInfo.getName().contains("Team")) {
            tournamentInfo.setLogo ("../assets/images/AmericasTeamChampionshipSmall.png");
        } else if (tournamentInfo.getName().contains("Aurora Cup")) {
            tournamentInfo.setLogo ("../assets/images/AuroraCupWhiteSmall.png");
        } else if (tournamentInfo.getName().contains("Badger")) {
            tournamentInfo.setLogo ("../assets/images/BadgerOpenSmall.png");
        } else {
            tournamentInfo.setLogo("../assets/images/MissingLogoSmall.png");
        }
        tournamentInfo.setTotalPrizeMoney(tournament.getTotalPrizeMoney());
        tournamentInfo.setReady(tournament.isReady());

        return tournamentInfo;
    }
}
