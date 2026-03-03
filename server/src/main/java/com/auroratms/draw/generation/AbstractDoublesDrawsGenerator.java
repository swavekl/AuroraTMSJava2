package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventRound;
import com.auroratms.event.TournamentEventRoundDivision;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.doubles.DoublesPair;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractDoublesDrawsGenerator extends AbstractDrawsGenerator {

    // teamed up players
    protected List<DoublesPair> doublesPairsForEvent;

    public AbstractDoublesDrawsGenerator(TournamentEvent tournamentEvent, TournamentEventRound tournamentEventRound, TournamentEventRoundDivision tournamentEventRoundDivision) {
        super(tournamentEvent, tournamentEventRound, tournamentEventRoundDivision);
    }

    public void setDoublesPairs(List<DoublesPair> doublesPairsForEvent) {
        this.doublesPairsForEvent = doublesPairsForEvent;
    }

    /**
     * Makes draw item for doubles team
     * @param doublesPair
     * @param eventFk
     * @param groupNum
     * @param rowNum
     * @param entryIdToPlayerDrawInfo
     * @param eventEntries
     * @return
     */
    protected DrawItem makeDoublesDrawItem(DoublesPair doublesPair, long eventFk, int groupNum, int rowNum,
                                           Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo,
                                           List<TournamentEventEntry> eventEntries, DrawType drawType) {
        DrawItem drawItem = null;
        Long playerAEventEntryFk = doublesPair.getPlayerAEventEntryFk();
        Long playerBEventEntryFk = doublesPair.getPlayerBEventEntryFk();
        long playerAEntryId = getPlayerEntryId(playerAEventEntryFk, eventEntries);
        long playerBEntryId = getPlayerEntryId(playerBEventEntryFk, eventEntries);
        PlayerDrawInfo playerADrawInfo = entryIdToPlayerDrawInfo.get(playerAEntryId);
        PlayerDrawInfo playerBDrawInfo = entryIdToPlayerDrawInfo.get(playerBEntryId);
        if (playerADrawInfo != null && playerBDrawInfo != null) {
            drawItem = new DrawItem();
            drawItem.setEventFk(eventFk);
            drawItem.setDrawType(drawType);
            drawItem.setPlayerId(playerADrawInfo.getProfileId() + ";" + playerBDrawInfo.getProfileId());
            drawItem.setPlayerName(playerADrawInfo.getPlayerName() + " / " + playerBDrawInfo.getPlayerName());
            drawItem.setRating(doublesPair.getSeedRating());
            String clubAName = (playerADrawInfo.getClubName() != null) ? playerADrawInfo.getClubName() : "N/A";
            String clubBName = (playerBDrawInfo.getClubName() != null) ? playerBDrawInfo.getClubName() : "N/A";
            drawItem.setClubName(clubAName + " / " + clubBName);
            String stateA = (playerADrawInfo.getState() != null) ? playerADrawInfo.getState() : "N/A";
            String stateB = (playerBDrawInfo.getState() != null) ? playerBDrawInfo.getState() : "N/A";
            drawItem.setState(stateA + " / " + stateB);
            drawItem.setGroupNum(groupNum);
            drawItem.setPlaceInGroup(rowNum);
            drawItem.setEntryId(playerAEntryId);
            drawItem.setDoublesPairId(doublesPair.getId());
            drawItem.setRoundOrdinalNumber(this.tournamentEventRound.getOrdinalNum());
            drawItem.setDivisionIdx(this.tournamentEventRoundDivision.getDivisionIdx());
        }
        return drawItem;
    }

    /**
     * Get player draw info
     * @param eventEntryFk
     * @param eventEntries
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    protected PlayerDrawInfo getPlayerDrawInfo(long eventEntryFk,
                                             List<TournamentEventEntry> eventEntries,
                                             Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        long entryId = getPlayerEntryId(eventEntryFk, eventEntries);
        return entryId == 0 ? null : entryIdToPlayerDrawInfo.get(entryId);
    }

    /**
     * Get player tournament entry id
     * @param eventEntryFk
     * @param eventEntries
     * @return
     */
    protected long getPlayerEntryId(long eventEntryFk,
                                    List<TournamentEventEntry> eventEntries) {
        return eventEntries.stream()
                .filter(tee -> Objects.equals(tee.getId(), eventEntryFk))
                .mapToLong(TournamentEventEntry::getTournamentEntryFk)
                .findFirst()
                .orElse(0L);
    }
}
