package com.auroratms.match;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates order of matches for a given number of players in a round robin event
 * Players are marked as A, B, C etc.  The bye is marked as '-'
 */
public class MatchOrderGenerator {

    public static final Character BYE = '-';

    /**
     * Generates order of matches for given number of players
     * @param playersDrawnIntoGroup
     * @param playersToAdvance
     * @return
     */
    public static List<MatchOpponents> generateOrderOfMatches(int playersDrawnIntoGroup, int playersToAdvance) {
        int sideSpots = playersDrawnIntoGroup / 2 + ((playersDrawnIntoGroup % 2 == 1) ? 1 : 0);
        // divide players into two groups left (A, B, C) & right (D, E, F)
        List<Character> leftSidePlayers = new ArrayList<>(sideSpots);
        List<Character> rightSidePlayers = new ArrayList<>(sideSpots);

        // place players A, B, C on the left side
        char playerLetter = 'A';
        for (int i = 0; i < sideSpots; i++) {
            leftSidePlayers.add(playerLetter);
            playerLetter++;
        }

        // place players D, E, F on the right.  If there is odd number of players last one will get bye indicated by '-'
        for (int i = 0; i < sideSpots; i++) {
            int playersPlaced = (int)playerLetter - (int)'A';
            char playerLetterToAdd = (playersPlaced < playersDrawnIntoGroup) ? playerLetter : BYE;
            rightSidePlayers.add(0, playerLetterToAdd);
            playerLetter++;
        }

        List<MatchOpponents> allMatchOpponents = new ArrayList<>();

        // calculate number of totalRounds
        int totalRounds = (sideSpots * 2) - 1;

        // match to avoid
        char avoidMatchPlayer1 = (char) ('A' + (playersToAdvance - 1));
        char avoidMatchPlayer2 = (char) ('B' + (playersToAdvance - 1));

        // rotate players in counter clockwise fashion to get the rounds opponents
        for (int round = 0; round < totalRounds; round++) {
            // first round is already setup so rotate only for later rounds
            if (round > 0) {
                rotateSpots(leftSidePlayers, rightSidePlayers);
            }
            // for more than one player to advance rotate twice after 1st and before last round
            boolean lastRound = round == (totalRounds - 1);
            if (playersToAdvance > 1) {
                boolean matchToBePlayed = isMatchToBePlayed(avoidMatchPlayer1, avoidMatchPlayer2, leftSidePlayers, rightSidePlayers);
                // check that B vs C match is not played in this round unless it is the last round
                if (matchToBePlayed && !lastRound) {
                    // avoid this match until the last round
                    rotateSpots(leftSidePlayers, rightSidePlayers);
                } else if (!matchToBePlayed && lastRound) {
                    // match needs to be played in the last round if it isn't, so rotate until it is played
                    do  {
                        rotateSpots(leftSidePlayers, rightSidePlayers);
                        matchToBePlayed = isMatchToBePlayed(avoidMatchPlayer1, avoidMatchPlayer2, leftSidePlayers, rightSidePlayers);
                    } while (!matchToBePlayed);
                }
            }

            List<MatchOpponents> matchOpponentsList = convertToMatchOpponentsList(leftSidePlayers, rightSidePlayers);

            // ensure that match to avoid is the match that is played last e.g. A vs B for 1 player advancing
            if (lastRound) {
                pushStrongestPlayersMatchToBePlayedLast(avoidMatchPlayer1, avoidMatchPlayer2, matchOpponentsList);
            }
            allMatchOpponents.addAll(matchOpponentsList);
        }
        return allMatchOpponents;
    }

    /**
     * Moves the match to be played last to the end
     * if 1 player advances it is A vs B (the two strongest players) that will play last match
     * if 2 players advance it is B vs C
     * @param avoidMatchPlayer1
     * @param avoidMatchPlayer2
     * @param matchOpponentsList
     * @return
     */
    private static void pushStrongestPlayersMatchToBePlayedLast(char avoidMatchPlayer1, char avoidMatchPlayer2, List<MatchOpponents> matchOpponentsList) {
        int index = 0;
        MatchOpponents removedMatchOpponents = null;
        for (MatchOpponents matchOpponents : matchOpponentsList) {
            if ((matchOpponents.playerALetter == avoidMatchPlayer1 && matchOpponents.playerBLetter == avoidMatchPlayer2) ||
                (matchOpponents.playerALetter == avoidMatchPlayer2 && matchOpponents.playerBLetter == avoidMatchPlayer1)) {
                removedMatchOpponents = matchOpponentsList.remove(index);
                break;
            }
            index++;
        }
        if (removedMatchOpponents != null) {
            matchOpponentsList.add(removedMatchOpponents);
        }
    }

    /**
     *
     * @param player1Code
     * @param player2Code
     * @param leftSidePlayers
     * @param rightSidePlayers
     * @return
     */
    private static boolean isMatchToBePlayed(char player1Code, char player2Code, List<Character> leftSidePlayers, List<Character> rightSidePlayers) {
        int sideSpots = leftSidePlayers.size();
        for (int i = 0; i < sideSpots; i++) {
            Character leftSidePlayer = leftSidePlayers.get(i);
            Character rightSidePlayer = rightSidePlayers.get(i);
            if ((leftSidePlayer == player1Code && rightSidePlayer == player2Code) ||
                (leftSidePlayer == player2Code && rightSidePlayer == player1Code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Rotates players in counter clockwise fashion, leaving A in the same spot
     *  A vs D    becomes  A vs C
     *  B vs C             D vs B
     *  @param leftSidePlayers
     * @param rightSidePlayers
     */
    private static void rotateSpots(List<Character> leftSidePlayers, List<Character> rightSidePlayers) {
        // remove the players
        Character topRightSidePlayer = rightSidePlayers.remove(0);
        Character bottomLeftSidePlayer = leftSidePlayers.remove(leftSidePlayers.size() - 1);

        // add players back
        leftSidePlayers.add(1, topRightSidePlayer);
        rightSidePlayers.add(bottomLeftSidePlayer);
    }

    /**
     * Converts left and righ side into match opponents
     * A vs D
     * B vs C
     *
     * @param leftSidePlayers
     * @param rightSidePlayers
     * @return
     */
    private static List<MatchOpponents> convertToMatchOpponentsList(List<Character> leftSidePlayers, List<Character> rightSidePlayers) {
        int sideSpots = leftSidePlayers.size();
        List<MatchOpponents> matchOpponentsList = new ArrayList<>(sideSpots);
        for (int i = 0; i < sideSpots; i++) {
            Character leftSidePlayer = leftSidePlayers.get(i);
            Character rightSidePlayer = rightSidePlayers.get(i);
            MatchOpponents matchOpponents = new MatchOpponents();
            matchOpponents.playerALetter = leftSidePlayer;
            matchOpponents.playerBLetter = rightSidePlayer;
            matchOpponentsList.add(matchOpponents);
        }

        return matchOpponentsList;
    }

}
