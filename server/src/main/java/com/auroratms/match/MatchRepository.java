package com.auroratms.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for persisting match results
 */
public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findAllByMatchCardOrderByMatchNum(MatchCard matchCard);
}
