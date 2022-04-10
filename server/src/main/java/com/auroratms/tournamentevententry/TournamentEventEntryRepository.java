package com.auroratms.tournamentevententry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TournamentEventEntryRepository extends JpaRepository<TournamentEventEntry, Long> {

    // gets all event entries for one tournament entry
    List<TournamentEventEntry> findByTournamentEntryFk(Long tournamentEntryFk);

    // gets all entries which were not finalized
    List<TournamentEventEntry> findAllByCartSessionId(String cartSessionUUID);

    // gets all event entries in all events of a tournament with one of specified statuses
    List<TournamentEventEntry> findAllByTournamentFkAndStatusInOrderByTournamentEntryFkAscTournamentEventFkAsc(Long tournamentFk, List<EventEntryStatus> statusList);

    // gets all event entries in one particular event that are ready for draws - i.e. confirmed
    List<TournamentEventEntry> findAllByTournamentEventFkEqualsAndStatusEquals(Long tournamentEventFk, EventEntryStatus eventEntryStatus);

    // gets all event entries for a particular tournament entry i.e. player
    List<TournamentEventEntry> findAllByTournamentEntryFk(Long tournamentEntryId);

    // count of all event entries in one particular event that are in several different statuses
    long countByTournamentEventFkEqualsAndStatusIn(Long tournamentEventFk, List<EventEntryStatus> statusList);

    // gets count of all event entries in all events of a tournament
    int countByTournamentFkEqualsAndStatusIn(Long tournamentEventFk, List<EventEntryStatus> statusList);

    @Query("select count(distinct tee.tournamentEntryFk) from TournamentEventEntry tee " +
            "where tee.tournamentFk = ?1 and tee.status in (?2)")
    int countTournamentEntries(Long tournamentFk, List<EventEntryStatus> statusList);

    // find all entries for players who have an event entry with given status (e.g. waiting list)
    @Query("from TournamentEventEntry tee2" +
            " where tee2.tournamentEntryFk in (" +
            "   select distinct tee.tournamentEntryFk" +
            "   from TournamentEventEntry tee" +
            "   where tee.tournamentFk = ?1 and tee.status = ?2" +
            ")"
    )
    List<TournamentEventEntry> findAllEntriesByTournamentFkWithEventStatus(Long tournamentFk, EventEntryStatus status);
}
