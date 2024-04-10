package com.auroratms.tournament;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class TournamentEventHelper {

    @Autowired
    TournamentEventEntityService tournamentEventEntityService;

    @Transactional
    public void cloneEvents(long fromTournamentId, Long clonedTournamentId) {
        Collection<TournamentEvent> list = tournamentEventEntityService.list(fromTournamentId, Pageable.unpaged());
        System.out.println("TournamentEventHelper.cloneEvents");
        List<TournamentEvent> clonedEventList = new ArrayList<>(list.size());
        for (TournamentEvent tournamentEvent : list) {
            TournamentEvent clonedTournamentEvent = new TournamentEvent(tournamentEvent);
            clonedTournamentEvent.setTournamentFk(clonedTournamentId);
            clonedTournamentEvent.setId(null);
            clonedTournamentEvent.setNumEntries(0);
            clonedEventList.add(clonedTournamentEvent);
        }
        tournamentEventEntityService.saveAll(clonedEventList);
        System.out.println("Saved clonedEventList events " + clonedEventList.size());
    }

    @Transactional
    public void deleteEvents(long tournamentId) {
        Collection<TournamentEvent> list = tournamentEventEntityService.list(tournamentId, Pageable.unpaged());
        for (TournamentEvent tournamentEvent : list) {
            tournamentEventEntityService.delete(tournamentEvent.getId());
        }
    }
}
