package com.auroratms.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    // finds all teams which entered any of the events listed in the list
    List<Team> findByTournamentEventFkIsIn(List<Long> eventIdsList);
}
