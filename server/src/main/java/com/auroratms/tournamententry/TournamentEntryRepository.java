package com.auroratms.tournamententry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
     * list tournament entries for player
     * @param profileId
     * @return
     */
    List<TournamentEntry> findAllByProfileId (String profileId);

    /**
     * Finds all entries that the
     * @param tournamentFk
     * @param profileId
     * @return
     */
    List<TournamentEntry> findByTournamentFkInAndProfileId (List<Long> tournamentFk, String profileId);

    /**
     * Lists all entries with given ids
     * @param tournamentEntryIds
     * @return
     */
    List<TournamentEntry> findAllByIdIn(List<Long> tournamentEntryIds);

    /**
     * Counts entries regardless of if there are any event entries
     * @param tournamentId
     * @return
     */
    int countTournamentEntryByTournamentFkEquals(long tournamentId);

    @Query("select te.id from TournamentEntry te " +
            "where te.tournamentFk = ?1 order by te.id")
    List<Long> findAllEntryIds(long tournamentId);
}
