package com.auroratms.match;

import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.error.ResourceUpdateFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing individual matches
 */
@Service
@Transactional
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    public MatchService() {
    }

    /**
     * Matches
     * @param matchId
     * @return
     */
    public Match getMatch(long matchId) {
        return this.matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find match"));
    }

    public Match updateMatch(Match match) {
        try {
            return this.matchRepository.save(match);
        } catch (Exception e) {
            throw new ResourceUpdateFailedException("Unable to update match results");
        }
    }

    public List<Match> getMatchesForCard(long matchCardId) {
        MatchCard example = new MatchCard();
        example.setId(matchCardId);
        return this.matchRepository.findAllByMatchCardOrderByMatchNum(example);
    }

    public List<Match> getMatchesForEvent(long eventId) {
        MatchCard example = new MatchCard();
        example.setEventFk(eventId);
        return this.matchRepository.findAllByMatchCardOrderByMatchNum(example);
    }
}
