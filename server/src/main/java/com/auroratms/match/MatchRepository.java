package com.auroratms.match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for persisting match results
 */
public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findAllByMatchCardOrderByMatchNum(MatchCard matchCard);

    List<Match> findAllByMatchCardIn(List<MatchCard> matchCards);

    @Modifying
    @Query("UPDATE Match" +
            " SET scoreEnteredByProfileId = :profileId " +
            " WHERE id = :matchId AND scoreEnteredByProfileId IS NULL"
    )
    int lockMatch(@Param("matchId") long matchId,
                  @Param("profileId") String profileId);

    List<Match> findAllByIdIn(List<Long> matchIds);
}
