package com.auroratms.team;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    // finds all teams which entered any of the events listed in the list
    List<Team> findByTournamentEventFkIsIn(List<Long> eventIdsList);

    // Fetch all teams for an event, AND their members in 1 query
    @EntityGraph(attributePaths = {"teamMembers"})
    List<Team> findAllByTournamentEventFk(long tournamentEventFk);
}
