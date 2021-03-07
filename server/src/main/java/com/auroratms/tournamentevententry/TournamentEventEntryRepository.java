package com.auroratms.tournamentevententry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentEventEntryRepository extends JpaRepository<TournamentEventEntry, Long> {

    // gets all event entries for one tournament entry
    List<TournamentEventEntry> findByTournamentEntryFk(Long tournamentEntryFk);

    // gets all entries in the tournament sorted by tournament entry and event
    List<TournamentEventEntry> findAllByTournamentFkOrderByTournamentEntryFkAscTournamentEventFkAsc(Long tournamentFk);

    long countByTournamentEventFkEqualsAndStatusIn(Long tournamentEventFk, List<EventEntryStatus> statusList);
}
