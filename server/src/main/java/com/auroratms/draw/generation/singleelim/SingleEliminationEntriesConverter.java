package com.auroratms.draw.generation.singleelim;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.generation.PlayerDrawInfo;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Takes top n-advancing players from the round robin draws and makes single elimination event
 * entries from them
 */
public class SingleEliminationEntriesConverter {

    /**
     *
     * @param rrDrawItems
     * @param rrEventEntries
     * @param tournamentEventEntity
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    public static List<TournamentEventEntry> generateSEEventEntriesFromDraws(List<DrawItem> rrDrawItems,
                                                                       List<TournamentEventEntry> rrEventEntries,
                                                                       TournamentEventEntity tournamentEventEntity,
                                                                       Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        List<TournamentEventEntry> seEventEntries = new ArrayList<>();
        int playersToAdvance = tournamentEventEntity.getPlayersToAdvance();
        boolean doubles = tournamentEventEntity.isDoubles();
        for (DrawItem rrDrawItem : rrDrawItems) {
            if (rrDrawItem.getPlaceInGroup() <= playersToAdvance) {
                String playerId = rrDrawItem.getPlayerId();
                if(!doubles) {
                    TournamentEventEntry eventEntry = findEventEntry(playerId, rrEventEntries, entryIdToPlayerDrawInfo);
                    if (eventEntry != null) {
                        seEventEntries.add(eventEntry);
                    }
                } else {
                    // find both player entries
                    TournamentEventEntry [] eventEntries = findDoublesEventEntries(playerId, rrEventEntries, entryIdToPlayerDrawInfo);
                    for (int i = 0; i < eventEntries.length; i++) {
                        TournamentEventEntry eventEntry = eventEntries[i];
                        if (eventEntry != null) {
                            seEventEntries.add(eventEntry);
                        }
                    }
                }
            }
        }

        return seEventEntries;
    }

    /**
     *
     * @param playerId
     * @param rrEventEntries
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    private static TournamentEventEntry findEventEntry(String playerId,
                                                       List<TournamentEventEntry> rrEventEntries,
                                                       Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        for (Map.Entry<Long, PlayerDrawInfo> playerDrawInfoEntry : entryIdToPlayerDrawInfo.entrySet()) {
            PlayerDrawInfo playerDrawInfo = playerDrawInfoEntry.getValue();
            if (playerDrawInfo.getProfileId().equals(playerId)) {
                Long entryId = playerDrawInfoEntry.getKey();
                for (TournamentEventEntry rrEventEntry : rrEventEntries) {
                    if (rrEventEntry.getTournamentEntryFk() == entryId) {
                        return rrEventEntry;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Doubles entry maker
     *
     * @param teamPlayerIds
     * @param rrEventEntries
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    private static TournamentEventEntry[] findDoublesEventEntries(String teamPlayerIds,
                                                                  List<TournamentEventEntry> rrEventEntries,
                                                                  Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        String[] playerIds = teamPlayerIds.split(";");
        TournamentEventEntry [] tournamentEventEntries = new TournamentEventEntry[playerIds.length];
        for (int i = 0; i < playerIds.length; i++) {
            String playerId = playerIds[i];
            for (Map.Entry<Long, PlayerDrawInfo> playerDrawInfoEntry : entryIdToPlayerDrawInfo.entrySet()) {
                PlayerDrawInfo playerDrawInfo = playerDrawInfoEntry.getValue();
                if (playerDrawInfo.getProfileId().equals(playerId)) {
                    Long entryId = playerDrawInfoEntry.getKey();
                    for (TournamentEventEntry rrEventEntry : rrEventEntries) {
                        if (rrEventEntry.getTournamentEntryFk() == entryId) {
                            tournamentEventEntries[i] = rrEventEntry;
                        }
                    }
                }
            }
        }

        return tournamentEventEntries;
    }
}
