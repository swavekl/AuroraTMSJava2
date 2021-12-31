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
    @Transactional(readOnly = true)
    public Match getMatch(long matchId) {
        return this.matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find match with id " + matchId));
    }

    public Match updateMatch(Match updatedMatch) {
        try {
            Match match = this.matchRepository.findById(updatedMatch.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unable to update updatedMatch with id " + updatedMatch.getId()));
            match.setSideADefaulted(updatedMatch.isSideADefaulted());
            match.setSideBDefaulted(updatedMatch.isSideBDefaulted());
            match.setGame1ScoreSideA(updatedMatch.getGame1ScoreSideA());
            match.setGame1ScoreSideB(updatedMatch.getGame1ScoreSideB());
            match.setGame2ScoreSideA(updatedMatch.getGame2ScoreSideA());
            match.setGame2ScoreSideB(updatedMatch.getGame2ScoreSideB());
            match.setGame3ScoreSideA(updatedMatch.getGame3ScoreSideA());
            match.setGame3ScoreSideB(updatedMatch.getGame3ScoreSideB());
            match.setGame4ScoreSideA(updatedMatch.getGame4ScoreSideA());
            match.setGame4ScoreSideB(updatedMatch.getGame4ScoreSideB());
            match.setGame5ScoreSideA(updatedMatch.getGame5ScoreSideA());
            match.setGame5ScoreSideB(updatedMatch.getGame5ScoreSideB());
            match.setGame6ScoreSideA(updatedMatch.getGame6ScoreSideA());
            match.setGame6ScoreSideB(updatedMatch.getGame6ScoreSideB());
            match.setGame7ScoreSideA(updatedMatch.getGame7ScoreSideA());
            match.setGame7ScoreSideB(updatedMatch.getGame7ScoreSideB());
            match.setSideAServesFirst(updatedMatch.isSideAServesFirst());
            match.setSideATimeoutTaken(updatedMatch.isSideATimeoutTaken());
            match.setSideBTimeoutTaken(updatedMatch.isSideBTimeoutTaken());
            return this.matchRepository.save(match);
        } catch (Exception e) {
            throw new ResourceUpdateFailedException("Unable to update updatedMatch results");
        }
    }

    @Transactional(readOnly = true)
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

    public List<Match> findAllByMatchCardIn(List<MatchCard> matchCardList) {
        return this.matchRepository.findAllByMatchCardIn(matchCardList);
    }
}
