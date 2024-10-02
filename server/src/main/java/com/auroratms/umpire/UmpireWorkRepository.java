package com.auroratms.umpire;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository for recording umpire and assistant umpire assignments i.e. work
 */
public interface UmpireWorkRepository extends JpaRepository<UmpireWork, Long> {

    List<UmpireWork> findByTournamentFk(long tournamentFk);

    @Query("FROM UmpireWork uwe" +
            " WHERE uwe.umpireProfileId = ?1 " +
            " OR uwe.assistantUmpireProfileId = ?1")
    List<UmpireWork> findByUmpireProfileId(String umpireProfileId);
}
