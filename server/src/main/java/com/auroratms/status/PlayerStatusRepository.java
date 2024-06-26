package com.auroratms.status;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for storing player status
 */
public interface PlayerStatusRepository extends JpaRepository<PlayerStatus, Long> {

    // All players for given tournament
    List<PlayerStatus> findAllByTournamentIdOrderByTournamentDay(long tournamentId);

    // All players for given tournament and day
    List<PlayerStatus> findAllByTournamentIdAndTournamentDay(long tournamentId, int tournamentDay);

    // one player for given tournament and day
    List<PlayerStatus> findAllByPlayerProfileIdAndTournamentIdAndTournamentDayAndEventId(
            String profileId, long tournamentId, int tournamentDay, long eventId);

    List<PlayerStatus> findAllByPlayerProfileIdIsInAndTournamentIdAndTournamentDayAndEventId(
            List<String> playerProfileIds, long tournamentId, int tournamentDay, long eventId);
}
