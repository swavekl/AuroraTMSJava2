package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.draw.generation.singleelim.BracketLine;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventRound;
import com.auroratms.event.TournamentEventRoundDivision;
import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class with common code for generators
 */
public abstract class AbstractDrawsGenerator {

    protected TournamentEvent tournamentEvent;

    protected TournamentEventRound tournamentEventRound;

    protected TournamentEventRoundDivision tournamentEventRoundDivision;

    public AbstractDrawsGenerator(TournamentEvent tournamentEvent, TournamentEventRound tournamentEventRound, TournamentEventRoundDivision tournamentEventRoundDivision) {
        this.tournamentEvent = tournamentEvent;
        this.tournamentEventRound = tournamentEventRound;
        this.tournamentEventRoundDivision = tournamentEventRoundDivision;
    }

    /**
     * Sorts submitted event entries list by player seed rating
     *
     * @param eventEntries            event entries
     * @param entryIdToPlayerDrawInfo player info with rating
     */
    protected void sortEntriesByRating(List<TournamentEventEntry> eventEntries,
                                       Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        AtomicBoolean dump = new AtomicBoolean(false);
        Collections.sort(eventEntries, new Comparator<TournamentEventEntry>() {
            @Override
            public int compare(TournamentEventEntry tee1, TournamentEventEntry tee2) {
                long tournamentEntryFk1 = tee1.getTournamentEntryFk();
                long tournamentEntryFk2 = tee2.getTournamentEntryFk();
                PlayerDrawInfo pdi1 = entryIdToPlayerDrawInfo.get(tournamentEntryFk1);
                PlayerDrawInfo pdi2 = entryIdToPlayerDrawInfo.get(tournamentEntryFk2);
                int rating1 = (pdi1 != null) ? pdi1.getRating() : 0;
                int rating2 = (pdi2 != null) ? pdi2.getRating() : 0;
                if (pdi1 == null) {
                    System.out.println("tournamentEntryFk1 has no player draw info " + tournamentEntryFk1 + " for event entry " + tee1.getTournamentEventFk());
                    dump.set(true);
                }
                if (pdi2 == null) {
                    System.out.println("tournamentEntryFk2 has no player draw info " + tournamentEntryFk2 + " for event entry " + tee2.getTournamentEventFk());
                    dump.set(true);
                }
                return Integer.compare(rating2, rating1);
            }
        });
        if (dump.get()) {
            dumpEntries(eventEntries, entryIdToPlayerDrawInfo);
        }
    }

    private void dumpEntries(List<TournamentEventEntry> eventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        System.out.println("Sorted entries - [TE id], rating player name");
        for (TournamentEventEntry eventEntry : eventEntries) {
            long tournamentEntryFk = eventEntry.getTournamentEntryFk();
            PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(tournamentEntryFk);
            String playerName = (playerDrawInfo != null) ? playerDrawInfo.getPlayerName() : "Unknown Player";
            int playerRating = (playerDrawInfo != null) ? playerDrawInfo.getRating() : 0;
            System.out.println (String.format("[%d] %d -> %s", tournamentEntryFk, playerRating, playerName));
        }
    }

    /**
     * Make a draw item with player information populated
     *
     * @param eventFk
     * @param groupNum
     * @param placeInGroup
     * @param playerDrawInfo
     * @param drawType
     * @param entryId
     * @return
     */
    protected DrawItem makeDrawItem(long eventFk,
                                    int groupNum,
                                    int placeInGroup,
                                    PlayerDrawInfo playerDrawInfo,
                                    DrawType drawType,
                                    long entryId) {
        DrawItem drawItem = new DrawItem();
        drawItem.setEventFk(eventFk);
        drawItem.setDrawType(drawType);
        drawItem.setPlayerId(playerDrawInfo.getProfileId());
        drawItem.setPlayerName(playerDrawInfo.getPlayerName());
        drawItem.setRating(playerDrawInfo.getRating());
        drawItem.setClubName(playerDrawInfo.getClubName());
        drawItem.setState(playerDrawInfo.getState());
        drawItem.setGroupNum(groupNum);
        drawItem.setPlaceInGroup(placeInGroup);
        drawItem.setEntryId(entryId);
        drawItem.setDoublesPairId(0L);
        drawItem.setRoundOrdinalNumber(this.tournamentEventRound.getOrdinalNum());
        drawItem.setDivisionIdx(this.tournamentEventRoundDivision.getDivisionIdx());
        return drawItem;
    }

    protected void dumpDraw(DrawItem[] drawItemsArray, BracketLine[] bracketLines) {
        System.out.println("Draw ====== START");
        for (int i = 0; i < drawItemsArray.length; i++) {
            DrawItem drawItem = drawItemsArray[i];
            BracketLine bracketLine = bracketLines[i];
            if (drawItem != null) {
                if (drawItem.getByeNum() == 0) {
                    System.out.println("%2d) %2d\t%25s,%4d\t\"%s\"".formatted((i + 1), bracketLine.getNormalizedSeedNumber(),
                            drawItem.getPlayerName(), drawItem.getRating(), drawItem.getState()));
                } else {
                    System.out.println("%2d) %2d\t%23s %d, 0".formatted((i + 1), bracketLine.getNormalizedSeedNumber(), "Bye", drawItem.getByeNum()));
                }
            } else {
                if (bracketLine.isBye()) {
                    System.out.println("%2d) %2d\t%23s %d, 0".formatted((i + 1), bracketLine.getNormalizedSeedNumber(), "Bye", bracketLine.getByeSeedNumber()));
                } else {
                    System.out.println("%2d) %2d".formatted((i + 1), bracketLine.getNormalizedSeedNumber()));
                }
            }
        }
        System.out.println("Draw ====== END");
    }

    /**
     * @param bracketLine
     * @return
     */
    protected DrawItem makeByeLine(BracketLine bracketLine) {
        DrawItem drawItem = new DrawItem();
        long eventFk = this.tournamentEvent.getId();
        drawItem.setEventFk(eventFk);
        drawItem.setDrawType(DrawType.SINGLE_ELIMINATION);
        drawItem.setGroupNum(bracketLine.getSeedNumber());
        drawItem.setByeNum(bracketLine.getByeSeedNumber());
        drawItem.setPlayerId("");
        drawItem.setRoundOrdinalNumber(this.tournamentEventRound.getOrdinalNum());
        drawItem.setDivisionIdx(this.tournamentEventRoundDivision.getDivisionIdx());
        return drawItem;
    }
}
