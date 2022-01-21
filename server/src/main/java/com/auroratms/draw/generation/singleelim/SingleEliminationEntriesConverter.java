package com.auroratms.draw.generation.singleelim;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.generation.PlayerDrawInfo;
import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Takes top n-advancing players from the round robin draws and makes single elimination event
 * entries from them
 */
public class SingleEliminationEntriesConverter {

    /**
     * @param rrDrawItems
     * @param rrEventEntries
     * @param tournamentEvent
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    public static List<TournamentEventEntry> generateSEEventEntriesFromDraws(List<DrawItem> rrDrawItems,
                                                                             List<TournamentEventEntry> rrEventEntries,
                                                                             TournamentEvent tournamentEvent,
                                                                             Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        List<TournamentEventEntry> seEventEntries = new ArrayList<>();
        int playersToAdvance = tournamentEvent.getPlayersToAdvance();
        boolean doubles = tournamentEvent.isDoubles();
        for (DrawItem rrDrawItem : rrDrawItems) {
            if (rrDrawItem.getPlaceInGroup() <= playersToAdvance) {
                String playerId = rrDrawItem.getPlayerId();
                if (!doubles) {
                    TournamentEventEntry eventEntry = findEventEntry(playerId, rrEventEntries, entryIdToPlayerDrawInfo);
                    if (eventEntry != null) {
                        seEventEntries.add(eventEntry);
                    }
                } else {
                    // find both player entries
                    TournamentEventEntry[] eventEntries = findDoublesEventEntries(playerId, rrEventEntries, entryIdToPlayerDrawInfo);
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
        TournamentEventEntry[] tournamentEventEntries = new TournamentEventEntry[playerIds.length];
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

    /**
     * Fill RR group number from which players came from
     *
     * @param rrDrawItems
     * @param entryIdToPlayerDrawInfo
     * @param tournamentEvent
     */
    public static void fillRRGroupNumberForSEPlayers(List<DrawItem> rrDrawItems,
                                                     Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo,
                                                     TournamentEvent tournamentEvent) {
        int playersToAdvance = tournamentEvent.getPlayersToAdvance();
        boolean doubles = tournamentEvent.isDoubles();
        for (DrawItem rrDrawItem : rrDrawItems) {
            if (rrDrawItem.getPlaceInGroup() <= playersToAdvance) {
                String playerId = rrDrawItem.getPlayerId();
                if (!doubles) {
                    PlayerDrawInfo playerDrawInfo = findPlayerDrawInfo(playerId, entryIdToPlayerDrawInfo);
                    if (playerDrawInfo != null) {
                        playerDrawInfo.setRRGroupNum(rrDrawItem.getGroupNum());
                    }
                } else {
                    PlayerDrawInfo [] playerDrawInfoArray = findDoublesTeamDrawInfo(playerId, entryIdToPlayerDrawInfo);
                    for (PlayerDrawInfo playerDrawInfo : playerDrawInfoArray) {
                        playerDrawInfo.setRRGroupNum(rrDrawItem.getGroupNum());
                    }
                }
            }
        }
    }

    /**
     * finds player draw info
     *
     * @param playerId
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    private static PlayerDrawInfo findPlayerDrawInfo(String playerId,
                                                     Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        PlayerDrawInfo retPlayerDrawInfo = null;
        Collection<PlayerDrawInfo> playerDrawInfos = entryIdToPlayerDrawInfo.values();
        for (PlayerDrawInfo playerDrawInfo : playerDrawInfos) {
            if (playerDrawInfo.getProfileId().equals(playerId)) {
                retPlayerDrawInfo = playerDrawInfo;
                break;
            }
        }

        return retPlayerDrawInfo;
    }

    /**
     * Finds player draw infos for each doubles team member
     * @param teamPlayerIds
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    private static PlayerDrawInfo[] findDoublesTeamDrawInfo(String teamPlayerIds,
                                                            Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        String[] playerIds = teamPlayerIds.split(";");
        PlayerDrawInfo[] playerDrawInfos = new PlayerDrawInfo[playerIds.length];
        for (int i = 0; i < playerIds.length; i++) {
            String playerId = playerIds[i];
            for (PlayerDrawInfo playerDrawInfo : entryIdToPlayerDrawInfo.values()) {
                if (playerDrawInfo.getProfileId().equals(playerId)) {
                    playerDrawInfos[i] = playerDrawInfo;
                    break;
                }
            }
        }

        return playerDrawInfos;
    }
}
