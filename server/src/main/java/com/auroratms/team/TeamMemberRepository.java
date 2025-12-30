package com.auroratms.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    /**
     * Finds all team memberships for a player restricted to specific events.
     * JOIN FETCH ensures the Team data is loaded in the same query to avoid N+1.
     */
    @Query("SELECT tm FROM TeamMember tm " +
            "JOIN FETCH tm.team t " +
            "WHERE tm.profileId = :profileId " +
            "AND t.tournamentEventFk IN :eventIds")
    List<TeamMember> findAllByProfileIdAndEventIds(@Param("eventIds") List<Long> eventIds,
                                                   @Param("profileId") String profileId
    );
}
