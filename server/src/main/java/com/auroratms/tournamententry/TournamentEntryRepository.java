package com.auroratms.tournamententry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentEntryRepository extends JpaRepository<TournamentEntry, Long> {

    /**
     * list of all tournament entries for tournament
     * @param tournamentId
     * @return
     */
    List<TournamentEntry> findAllByTournamentFk(Long tournamentId);

    /**
     * list of 1 or 0 of tournament entries for player
     * @param tournamentFk
     * @param profileId
     * @return
     */
    List<TournamentEntry> findByTournamentFkAndProfileId (Long tournamentFk, String profileId);

    /**
     * Counts entries regardless of if there are any event entries
     * @param tournamentId
     * @return
     */
    int countTournamentEntryByTournamentFkEquals(long tournamentId);
}
