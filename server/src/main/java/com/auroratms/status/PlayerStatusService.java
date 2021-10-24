package com.auroratms.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlayerStatusService {

    @Autowired
    private PlayerStatusRepository playerStatusRepository;

    public PlayerStatus save(PlayerStatus playerStatus) {
        return playerStatusRepository.save(playerStatus);
    }

    public List<PlayerStatus> listAllPlayers(long tournamentId, int tournamentDay) {
        return playerStatusRepository.findAllByTournamentIdAndTournamentDay(tournamentId, tournamentDay);
    }

    public List<PlayerStatus> listPlayersByIds(List<String> profileIdList, long tournamentId, int tournamentDay) {
        return playerStatusRepository.findAllByPlayerProfileIdIsInAndTournamentIdAndTournamentDay(
                profileIdList, tournamentId, tournamentDay);
    }

    public List<PlayerStatus> listOnePlayer(String profileId, long tournamentId, int tournamentDay) {
        return playerStatusRepository.findAllByPlayerProfileIdAndTournamentIdAndTournamentDay(profileId, tournamentId, tournamentDay);
    }
}
