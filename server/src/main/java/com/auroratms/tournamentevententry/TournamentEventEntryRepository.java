package com.auroratms.tournamentevententry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentEventEntryRepository extends JpaRepository<TournamentEventEntry, Long> {

    List<TournamentEventEntry> findByTournamentEntryFk(Long tournamentEntryFk);
}
