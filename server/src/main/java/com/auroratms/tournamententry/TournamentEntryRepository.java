package com.auroratms.tournamententry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentEntryRepository extends JpaRepository<TournamentEntry, Long> {

    List<TournamentEntry> findByTournamentFkAndProfileId (Long tournamentFk, String profileId);
}
