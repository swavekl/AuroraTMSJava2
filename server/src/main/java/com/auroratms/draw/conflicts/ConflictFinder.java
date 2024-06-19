package com.auroratms.draw.conflicts;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.draw.generation.PlayerDrawInfo;
import com.auroratms.event.DrawMethod;
import com.auroratms.event.TournamentEvent;
import com.auroratms.match.MatchOpponents;
import com.auroratms.match.MatchOrderGenerator;
import org.thymeleaf.util.StringUtils;

import java.util.*;

/**
 * Class for finding draw conflicts
 */
public class ConflictFinder {

    private final DrawType drawType;
    private final Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo;
    private final TournamentEvent tournamentEvent;

    private Map<String, List<DrawItem>> sortedExistingDrawItems;

    public ConflictFinder(DrawType drawType, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo,
                          List<DrawItem> existingDrawItems, TournamentEvent tournamentEvent) {
        this.drawType = drawType;
        this.entryIdToPlayerDrawInfo = entryIdToPlayerDrawInfo;
        this.tournamentEvent = tournamentEvent;
        this.sortedExistingDrawItems = organizeDrawsForSearching(existingDrawItems);
    }

    /**
     * Organizes other event draws by event - draw type - group
     * @param drawItems
     * @return
     */
    private Map<String, List<DrawItem>> organizeDrawsForSearching(List<DrawItem> drawItems) {
        Map<String, List<DrawItem>> groupIdToDrawItemsMap = new HashMap<>();
        for (DrawItem drawItem : drawItems) {
            String key = makeKey(drawItem);
            List<DrawItem> groupDrawItems = groupIdToDrawItemsMap.computeIfAbsent(key, k -> new ArrayList<>());
            groupDrawItems.add(drawItem);
            groupIdToDrawItemsMap.put(key, groupDrawItems);
        }

        return groupIdToDrawItemsMap;
    }

    private String makeKey(DrawItem drawItem) {
        return drawItem.getEventFk() + (drawType.equals(DrawType.ROUND_ROBIN) ? "-RR-" : "-SE-")
                + drawItem.getGroupNum() + " Round of " + drawItem.getRound();
    }


    /**
     * Main method for identifying conflicts
     * @param drawItems
     */
    public void identifyConflicts(List<DrawItem> drawItems) {

        Map<String, List<DrawItem>> thisEventDraws = organizeDrawsForSearching(drawItems);

        Map<String, ConflictInfo> playerConflictInfos = new HashMap<>();

        // assume single elimination round is always advancing 1 player, RR can be one or more
        int playersToAdvance = (drawType == DrawType.SINGLE_ELIMINATION) ? 1 : this.tournamentEvent.getPlayersToAdvance();
        Set<String> keySet = thisEventDraws.keySet();
        for (String key : keySet) {
            List<DrawItem> groupDrawItems = thisEventDraws.get(key);
            int playersDrawnIntoGroup = groupDrawItems.size();
            // if this is a RR group with seeded 1 player then skip it.  Otherwise, find conflicts
            if ((drawType == DrawType.ROUND_ROBIN && groupDrawItems.size() > 1) || drawType == DrawType.SINGLE_ELIMINATION) {
//                System.out.println("----------------------- group num: " + groupDrawItems.get(0).getGroupNum());
                // get a list of matches to be played so we can check all combinations of opponents for each player
                List<MatchOpponents> matchesInOrder = MatchOrderGenerator.generateOrderOfMatches(playersDrawnIntoGroup, playersToAdvance);
                // collect players information
                Map<Character, DrawItem> mapPlayerCodeToDrawItem = new HashMap<>();
                for (DrawItem drawItem : groupDrawItems) {
                    int placeInGroup = drawItem.getPlaceInGroup();
                    Character playerCode = (char) ('A' + (placeInGroup - 1));
                    mapPlayerCodeToDrawItem.put(playerCode, drawItem);
                }

                for (MatchOpponents matchOpponents : matchesInOrder) {
                    if (matchOpponents.playerALetter != MatchOrderGenerator.BYE &&
                            matchOpponents.playerBLetter != MatchOrderGenerator.BYE) {
                        DrawItem playerADrawItem = mapPlayerCodeToDrawItem.get(matchOpponents.playerALetter);
                        DrawItem playerBDrawItem = mapPlayerCodeToDrawItem.get(matchOpponents.playerBLetter);
                        if (playerADrawItem != null && playerBDrawItem != null) {
                            String playerAProfileId = playerADrawItem.getPlayerId();
                            String playerBProfileId = playerBDrawItem.getPlayerId();
                            Long entryIdA = playerADrawItem.getEntryId();
                            Long entryIdB = playerBDrawItem.getEntryId();

                            PlayerDrawInfo playerADrawInfo = this.entryIdToPlayerDrawInfo.get(entryIdA);
                            PlayerDrawInfo playerBDrawInfo = this.entryIdToPlayerDrawInfo.get(entryIdB);
                            if (playerADrawInfo != null && playerBDrawInfo != null) {
                                ConflictInfo playerAConflictInfo = playerConflictInfos.computeIfAbsent(playerAProfileId, k -> new ConflictInfo());
                                ConflictInfo playerBConflictInfo = playerConflictInfos.computeIfAbsent(playerBProfileId, l -> new ConflictInfo());
                                playerConflictInfos.put(playerAProfileId, playerAConflictInfo);
                                playerConflictInfos.put(playerBProfileId, playerBConflictInfo);
//                                System.out.println("--------------------------------------------------");
//                                System.out.println("playerA = " + playerADrawInfo.getPlayerName());
//                                System.out.println("playerB = " + playerBDrawInfo.getPlayerName());

                                // check if players live in the same state
                                boolean livesNearby = (!StringUtils.isEmpty(playerADrawInfo.getState()))
                                        ? StringUtils.equals(playerADrawInfo.getState(), playerBDrawInfo.getState())
                                        : false;
//                                System.out.println("playerADrawInfo.state = " + playerADrawInfo.getState());
//                                System.out.println("playerBDrawInfo.state = " + playerBDrawInfo.getState());
//                                System.out.println("livesNearby = " + livesNearby);
                                playerAConflictInfo.setLivesNearby(livesNearby);
                                playerBConflictInfo.setLivesNearby(livesNearby);

                                boolean playsInTheSameClub = playerADrawInfo.getClubId() != 0 &&
                                        playerADrawInfo.getClubId() == playerBDrawInfo.getClubId();
//                                System.out.println("playerADrawInfo.clubid = " + playerADrawInfo.getClubId());
//                                System.out.println("playerBDrawInfo.clubid = " + playerBDrawInfo.getClubId());
//                                System.out.println("playsInTheSameClub = " + playsInTheSameClub);

                                playerAConflictInfo.setPlaysInTheSameClub(playsInTheSameClub);
                                playerBConflictInfo.setPlaysInTheSameClub(playsInTheSameClub);

                                boolean playsOtherPlayerInAnotherEventRR = checkOtherEventsRR(playerAProfileId, playerBProfileId);
                                playerAConflictInfo.setPlaysInOtherEvent(playsOtherPlayerInAnotherEventRR);
                                playerBConflictInfo.setPlaysInOtherEvent(playsOtherPlayerInAnotherEventRR);
                            } else {
                                if (playerADrawInfo == null) {
                                    System.out.println("not found player info for player (A) id = " + playerAProfileId);
                                }
                                if (playerBDrawInfo == null) {
                                    System.out.println("not found player info for player (B) id = " + playerBProfileId);
                                }
                            }
                        }
                    }
                }

                // finally save conflicts to draw item
                for (DrawItem drawItem : groupDrawItems) {
                    String playerId = drawItem.getPlayerId();
                    ConflictInfo conflictInfo = playerConflictInfos.get(playerId);
                    if (conflictInfo != null) {
                        conflictInfo.computeFinalConflict(drawType, tournamentEvent.isSingleElimination(),
                                playersToAdvance, tournamentEvent.getDrawMethod());
                        drawItem.setConflictType(conflictInfo.getConflictType());
                    } else {
                        drawItem.setConflictType(new ConflictInfo().getConflictType());
                    }
                }
            } else if (groupDrawItems.size() == 1) {
                DrawItem drawItem = groupDrawItems.get(0);
                drawItem.setConflictType(new ConflictInfo().getConflictType());
            }
        }
    }

    private boolean checkOtherEventsRR(String playerAProfileId, String playerBProfileId) {
        boolean playEachOtherInOtherEvent = false;
        for (String key : sortedExistingDrawItems.keySet()) {
            List<DrawItem> groupDrawItems = sortedExistingDrawItems.get(key);
            boolean playerAInGroup = false;
            boolean playerBInGroup = false;
            for (DrawItem groupDrawItem : groupDrawItems) {
                if (playerAProfileId.equals(groupDrawItem.getPlayerId())) {
                    playerAInGroup = true;
                }
                if (playerBProfileId.equals(groupDrawItem.getPlayerId())) {
                    playerBInGroup = true;
                }
            }
            if (playerAInGroup && playerBInGroup) {
                playEachOtherInOtherEvent = true;
                break;
            }
        }
        return playEachOtherInOtherEvent;
    }

    /**
     * Local class for computing final conflict type
     */
    private static class ConflictInfo {

        // final type of conflict
        ConflictType conflictType = ConflictType.NO_CONFLICT;

        boolean livesNearby;

        boolean playsInTheSameClub;

        boolean playsInOtherEvent;

        public void setLivesNearby(boolean livesNearby) {
            this.livesNearby |= livesNearby;
        }

        public void setPlaysInTheSameClub(boolean playsInTheSameClub) {
            this.playsInTheSameClub |= playsInTheSameClub;
        }

        public void setPlaysInOtherEvent(boolean playsInOtherEvent) {
            this.playsInOtherEvent |= playsInOtherEvent;
        }

        public void computeFinalConflict (DrawType drawType, boolean isSingleEliminationEvent, int playerToAdvance, DrawMethod drawMethod) {
            if (!isSingleEliminationEvent) {
                // round robin event
                if (playsInTheSameClub) {
                    if (drawType == DrawType.ROUND_ROBIN) {
                        if (drawMethod == DrawMethod.SNAKE) {
                            conflictType = ConflictType.SAME_CLUB_FIRST_ROUND;
                        }
                    } else if (drawType == DrawType.SINGLE_ELIMINATION) {
                        conflictType = ConflictType.SAME_CLUB_SECOND_ROUND;
                    }
                } else if (playsInOtherEvent) {
                    if ((drawType == DrawType.ROUND_ROBIN && drawMethod == DrawMethod.SNAKE)
                            || drawType == DrawType.SINGLE_ELIMINATION) {
                        conflictType = ConflictType.PLAYS_IN_OTHER_EVENT_FIRST_ROUND;
                    }
                } else if (livesNearby) {
                    if ((drawType == DrawType.ROUND_ROBIN && drawMethod == DrawMethod.SNAKE)
                            || drawType == DrawType.SINGLE_ELIMINATION) {
                        conflictType = ConflictType.LIVES_NEARBY;
                    }
                }
            } else {
                if (playsInTheSameClub) {
                    conflictType = ConflictType.SAME_CLUB_SECOND_ROUND;
                } else if (playsInOtherEvent) {
                    conflictType = ConflictType.PLAYS_IN_OTHER_EVENT_FIRST_ROUND;
                } else if (livesNearby) {
                    conflictType = ConflictType.LIVES_NEARBY;
                }
            }
        }

        public ConflictType getConflictType() {
            return conflictType;
        }
    }
}
