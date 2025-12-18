package com.auroratms.team;

import com.auroratms.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    @Autowired
    private TeamRepository repository;

    public Team save(Team team) {
        return repository.save(team);
    }

    public Team get(Long teamId) {
        return repository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team " + teamId + " not found"));
    }

    /**
     * Lists all teams which entered one of the events
     * @param eventIdsList
     * @return
     */
    public List<Team> listForEvents(List<Long> eventIdsList) {
        return repository.findByTournamentEventFkIsIn(eventIdsList);
    }
}
