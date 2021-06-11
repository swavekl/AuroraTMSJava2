package com.auroratms.tournamentevententry.doubles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for managing doubles entries
 */
public interface DoublesRepository extends JpaRepository<DoublesPair, Long> {

    /**
     * Finds all player entries into this doubles event
     * @param tournamentEventFk
     * @return
     */
    List<DoublesPair> findDoublesPairsByTournamentEventFkOrderBySeedRatingDesc(long tournamentEventFk);

    /**
     * Get existing entry for this player in this doubles event (either player A or B)
     * @param tournamentEventFk
     * @param playerEventEntryId
     * @return
     */
    @Query(nativeQuery = true,
            value = "SELECT * FROM doublespair " +
                    " WHERE tournament_event_fk = :tournamentEventFk AND " +
                    "(playeraevent_entry_fk = :playerEventEntryId OR playerbevent_entry_fk = :playerEventEntryId);"
    )
    List<DoublesPair> findPlayerDoublesEntry(@Param("tournamentEventFk") Long tournamentEventFk,
                                             @Param("playerEventEntryId") Long playerEventEntryId);

    /**
     * Deletes doubles pair if either one or the other i
     * @param playerEventEntryId
     */
    @Query(nativeQuery = true,
            value = "DELETE FROM doublespair " +
                    " WHERE playeraevent_entry_fk = :playerEventEntryId OR playerbevent_entry_fk = :playerEventEntryId;"
    )
    void deleteByPlayerEventEntryId(@Param("playerEventEntryId") Long playerEventEntryId);

}
