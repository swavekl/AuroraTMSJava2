package com.auroratms.status;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawService;
import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class PlayerStatusService {

    @Autowired
    private PlayerStatusRepository playerStatusRepository;

    @Autowired
    private DrawService drawService;

    @Autowired
    private TournamentEventEntityService eventService;

    public PlayerStatus save(PlayerStatus playerStatus) {
        return playerStatusRepository.save(playerStatus);
    }

    public List<PlayerStatus> listAllPlayers(long tournamentId, int tournamentDay) {
        return playerStatusRepository.findAllByTournamentIdAndTournamentDay(tournamentId, tournamentDay);
    }

    public List<PlayerStatus> listPlayersByIds(List<String> profileIdList, long tournamentId, int tournamentDay, long eventId) {
        return playerStatusRepository.findAllByPlayerProfileIdIsInAndTournamentIdAndTournamentDayAndEventId(
                profileIdList, tournamentId, tournamentDay, eventId);
    }

    public List<PlayerStatus> listPlayersForEvent(long tournamentId, int tournamentDay, long eventId, Boolean isDailyCheckin) {
        TournamentEvent tournamentEvent = this.eventService.get(eventId);
        DrawType drawType = (tournamentEvent.isSingleElimination()) ? DrawType.SINGLE_ELIMINATION : DrawType.ROUND_ROBIN;
        List<DrawItem> drawItems = this.drawService.list(eventId, drawType);
        eventId = (isDailyCheckin) ? 0 : eventId;

        // now enhance this information with player name, club name and state
        // get profiles of players in this event
        List<String> profileIds = new ArrayList<>(drawItems.size());
        for (DrawItem drawItem : drawItems) {
            String profileId = drawItem.getPlayerId();
            // doubles event has playerA/playerB profile ids
            if (tournamentEvent.isDoubles()) {
                String[] playersProfileIds = profileId.split(";");
                profileIds.addAll(Arrays.asList(playersProfileIds));
            } else {
                profileIds.add(profileId);
            }
        }
        return this.listPlayersByIds(profileIds, tournamentId, tournamentDay, eventId);
    }

    public List<PlayerStatus> listOnePlayer(String profileId, long tournamentId, int tournamentDay, long eventId) {
        return playerStatusRepository.findAllByPlayerProfileIdAndTournamentIdAndTournamentDayAndEventId(
                profileId, tournamentId, tournamentDay, eventId);
    }
}
