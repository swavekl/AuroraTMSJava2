package com.auroratms.draw.generation.singleelim;

import com.auroratms.draw.generation.PlayerDrawInfo;
import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.*;

/**
 * Sorting utility which sorts event entries by club/city/state/rating
 * with largest group of players from the same club first, second largest group next etc.
 */
public class EntrySorter {

    private Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo;

    public EntrySorter(Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        this.entryIdToPlayerDrawInfo = entryIdToPlayerDrawInfo;
    }

    public List<TournamentEventEntry> sortEntries(List<TournamentEventEntry> entries, int requiredByes) {
        // wrap entries into objects that have more information
        List<PlayerEntryWrapper> playerEntryWrappers = new ArrayList<>(entries.size());
        for (TournamentEventEntry entry : entries) {
            long tournamentEntryFk = entry.getTournamentEntryFk();
            PlayerDrawInfo pdi = entryIdToPlayerDrawInfo.get(tournamentEntryFk);
            PlayerEntryWrapper playerEntryWrapper = new PlayerEntryWrapper(pdi, entry);
            playerEntryWrappers.add(playerEntryWrapper);
        }

        boolean useClub = assignCountOfPlayers(playerEntryWrappers);

        // sort players according to the rating from highest to lowest
        Comparator<PlayerEntryWrapper> ratingsComparator = Comparator
                .comparing(PlayerEntryWrapper::getRating).reversed();
        Collections.sort(playerEntryWrappers, ratingsComparator);

        // assign seed numbers 1, 2, 3 for 3 & 4, 5 for 5 - 8, etc.
        assignSeedNumbers (playerEntryWrappers);

        assignRankingWithinGeography(playerEntryWrappers, useClub);

        if (useClub) {
            // sort remaining players according to
            // sort seed number e.g. 3s, followed by 5s etc.
            // within each seed group by number of players from the club
            // then by club name alphabetically
            // then by rating within a club
            Comparator<PlayerEntryWrapper> comparator = Comparator
                    .comparing(PlayerEntryWrapper::getSeedNumber)
                    .thenComparing(Comparator.comparing(PlayerEntryWrapper::getCountOfPlayers).reversed())
                    .thenComparing(Comparator.comparing(PlayerEntryWrapper::getAlreadyPlacedFromGeography).reversed())
                    .thenComparing(PlayerEntryWrapper::getClub)
                    .thenComparing(Comparator.comparing(PlayerEntryWrapper::getRating).reversed());


            Collections.sort(playerEntryWrappers, comparator);
        } else {
            // use state
            // sort seed number e.g. 3s, followed by 5s etc.
            // within each seed group by number of players from that state decreasing
            // then by state name alphabetically
            // then by rating within state decreasing
            Comparator<PlayerEntryWrapper> comparator = Comparator
                    .comparing(PlayerEntryWrapper::getSeedNumber)
                    .thenComparing(Comparator.comparing(PlayerEntryWrapper::getCountOfPlayers).reversed())
                    .thenComparing(Comparator.comparing(PlayerEntryWrapper::getAlreadyPlacedFromGeography).reversed())
                    .thenComparing(PlayerEntryWrapper::getState)
                    .thenComparing(Comparator.comparing(PlayerEntryWrapper::getRating).reversed());

            Collections.sort(playerEntryWrappers, comparator);
        }


        // extract sorted entries
        List<TournamentEventEntry> sortedEntries = new ArrayList<>(entries.size());
        for (PlayerEntryWrapper playerEntryWrapper : playerEntryWrappers) {
            sortedEntries.add(playerEntryWrapper.getTournamentEventEntry());
            System.out.println(String.format("\"%25s\",\t%d, \"%s\", \"%s\", \"%s\", %2d, %2d",
                    playerEntryWrapper.getPlayerName(), playerEntryWrapper.getRating(),
                    playerEntryWrapper.getClub(), playerEntryWrapper.getCity(), playerEntryWrapper.getState(),
                    playerEntryWrapper.getSeedNumber(), playerEntryWrapper.getCountOfPlayers()));
        }

        return sortedEntries;
    }

    /**
     * Assigns a rank within geography
     * @param playerEntryWrappers
     * @param useClub
     */
    private void assignRankingWithinGeography(List<PlayerEntryWrapper> playerEntryWrappers, boolean useClub) {
        Map<String, Integer> geographyToCurrentRank = new HashMap<>();
        Map<String, Map<Integer, Integer>> geographyToAlreadyPlacedCount = new HashMap<>();
        for (PlayerEntryWrapper playerEntryWrapper : playerEntryWrappers) {
            String geography = (useClub) ? playerEntryWrapper.getClub() : playerEntryWrapper.getState();

            Integer rank = geographyToCurrentRank.get(geography);
            if (rank == null) {
                rank = 1;
            }
            playerEntryWrapper.setRankInGeography(rank);
            rank++;
            geographyToCurrentRank.put(geography, rank);

            // map of how many players with seed #5, from this geography, #9 from this geography
            Map<Integer, Integer> normalizedSeedCountsForGeography = geographyToAlreadyPlacedCount.get(geography);
            if (normalizedSeedCountsForGeography == null) {
                normalizedSeedCountsForGeography = new HashMap<>();
                geographyToAlreadyPlacedCount.put(geography, normalizedSeedCountsForGeography);
            }
            int playerNormalizedSeed = playerEntryWrapper.getSeedNumber();
            Integer countForNormalizedSeed = normalizedSeedCountsForGeography.get(playerNormalizedSeed);
            if (countForNormalizedSeed == null) {
                countForNormalizedSeed = 0;
            }
            countForNormalizedSeed++;
            normalizedSeedCountsForGeography.put(playerNormalizedSeed, countForNormalizedSeed);
        }

        // now assign a number which shows how many players from this geography were already placed before this player
        for (PlayerEntryWrapper playerEntryWrapper : playerEntryWrappers) {
            String geography = (useClub) ? playerEntryWrapper.getClub() : playerEntryWrapper.getState();
            int alreadyPlacedFromGeography = 0;
            Map<Integer, Integer> normalizedSeedCountsForGeography = geographyToAlreadyPlacedCount.get(geography);
            if (normalizedSeedCountsForGeography != null) {
                int playerNormalizedSeed = playerEntryWrapper.getSeedNumber();
                for (Integer normalizedSeed : normalizedSeedCountsForGeography.keySet()) {
                    if (normalizedSeed < playerNormalizedSeed) {
                        Integer count = normalizedSeedCountsForGeography.get(normalizedSeed);
                        count = (count != null) ? count : 0;
                        alreadyPlacedFromGeography += count;
                    }
                }
            }

//            System.out.println(playerEntryWrapper.getPlayerName() + " seed " + playerEntryWrapper.getSeedNumber() + " from state " + playerEntryWrapper.getState() + " alreadyPlacedFromGeography = " + alreadyPlacedFromGeography);
            playerEntryWrapper.setAlreadyPlacedFromGeography(alreadyPlacedFromGeography);
        }
    }

    /**
     * Assigns normalized seed numbers to each player
     * @param playerEntryWrappers
     */
    private void assignSeedNumbers(List<PlayerEntryWrapper> playerEntryWrappers) {
        int seedNumber = 1;
        for (PlayerEntryWrapper playerEntryWrapper : playerEntryWrappers) {
            int normalizedSeedNumber = getNormalizedSeedNumber(seedNumber);
            playerEntryWrapper.setSeedNumber(normalizedSeedNumber);
            seedNumber++;
        }
    }

    /**
     *
     * @param seedNumber
     * @return
     */
    private int getNormalizedSeedNumber(int seedNumber) {
        int normalizedSeedNumber = 0;
        if (seedNumber == 1 || seedNumber == 2) {
            normalizedSeedNumber = seedNumber;
        } else if (3 <= seedNumber && seedNumber <= 4) {
            normalizedSeedNumber = 3;
        } else if (5 <= seedNumber && seedNumber <= 8) {
            normalizedSeedNumber = 5;
        } else if (9 <= seedNumber && seedNumber <= 16) {
            normalizedSeedNumber = 9;
        } else if (17 <= seedNumber && seedNumber <= 32) {
            normalizedSeedNumber = 17;
        } else if (33 <= seedNumber && seedNumber <= 64) {
            normalizedSeedNumber = 33;
        } else if (65 <= seedNumber && seedNumber <= 128) {
            normalizedSeedNumber = 65;
        } else if (129 <= seedNumber && seedNumber <= 256) {
            normalizedSeedNumber = 129;
        } else {
            throw new RuntimeException("Unamble to compute normalized seed number for seed " + seedNumber);
        }
        return normalizedSeedNumber;
    }

    /**
     * Counts how many players are in each club and assigns each player from that club that number
     * @param playerEntryWrappers
     */
    private boolean assignCountOfPlayers(List<PlayerEntryWrapper> playerEntryWrappers) {
        // count players in the clubs to find those clubs which have the most players
        Map<String, Integer> clubToCountOfPlayers = new HashMap<>();
        Map<String, Integer> stateToCountOfPlayers = new HashMap<>();
        for (PlayerEntryWrapper playerEntryWrapper : playerEntryWrappers) {
            String club = playerEntryWrapper.getClub();
            if (club != null) {
                Integer countOfPlayers = clubToCountOfPlayers.get(club);
                if (countOfPlayers == null) {
                    countOfPlayers = 0;
                }
                countOfPlayers++;
                clubToCountOfPlayers.put(club, countOfPlayers);
            }
            // count states
            String state = playerEntryWrapper.getState();
            if (state != null) {
                Integer countOfPlayersPerState = stateToCountOfPlayers.get(state);
                if (countOfPlayersPerState == null) {
                    countOfPlayersPerState = 0;
                }
                countOfPlayersPerState++;
                stateToCountOfPlayers.put(state, countOfPlayersPerState);
            }
        }

        // if there are clubs use clubs members counts
        boolean useClub = (clubToCountOfPlayers.size() > 1);
        if (useClub) {
            // assign count to each player to be able to sort entries by it
            for (PlayerEntryWrapper playerEntryWrapper : playerEntryWrappers) {
                String club = playerEntryWrapper.getClub();
                if (club != null) {
                    Integer countOfPlayers = clubToCountOfPlayers.get(club);
                    playerEntryWrapper.setCountOfPlayers(countOfPlayers);
                } else {
                    playerEntryWrapper.setCountOfPlayers(0);
                }
            }
        } else {
            // otherwise use state separation
            for (PlayerEntryWrapper playerEntryWrapper : playerEntryWrappers) {
                String state = playerEntryWrapper.getState();
                if (state != null) {
                    Integer countOfPlayers = stateToCountOfPlayers.get(state);
                    playerEntryWrapper.setCountOfPlayers(countOfPlayers);
                } else {
                    playerEntryWrapper.setCountOfPlayers(0);
                }
            }
        }
        return useClub;
    }

    /**
     * Wrapper for associating tournament entry with player information
     */
    private class PlayerEntryWrapper {
        PlayerDrawInfo pdi;
        TournamentEventEntry tournamentEventEntry;
        // count of players from the same club
        int countOfPlayers;

        // 1, 2, 3, 5, 9, 17 etc.
        int seedNumber;

        // numeric rank within a geographical separation unit (state e.g. NJ-1, NJ-2) or club FVTTC-1, FVTTC-2 etc.
        int rankInGeography;

        // number players from this geography who were placed into the draw before this player
        int alreadyPlacedFromGeography;

        public PlayerEntryWrapper(PlayerDrawInfo pdi, TournamentEventEntry tee) {
            this.pdi = pdi;
            this.tournamentEventEntry = tee;
        }

        public int getRating () {
            return this.pdi.getRating();
        }

        public String getClub () {
            return (this.pdi.getClubName() != null) ? this.pdi.getClubName() : "";
        }

        public String getCity () {
            return (this.pdi.getCity() != null) ? this.pdi.getCity() : "";
        }

        public String getState () {
            return (this.pdi.getState() != null) ? this.pdi.getState() : "";
        }

        public TournamentEventEntry getTournamentEventEntry() {
            return tournamentEventEntry;
        }

        public int getCountOfPlayers() {
            return countOfPlayers;
        }

        public void setCountOfPlayers(int countOfPlayers) {
            this.countOfPlayers = countOfPlayers;
        }

        public String getPlayerName () {
            return this.pdi.getPlayerName();
        }

        public int getSeedNumber() {
            return seedNumber;
        }

        public void setSeedNumber(int seedNumber) {
            this.seedNumber = seedNumber;
        }

        public int getRankInGeography() {
            return rankInGeography;
        }

        public void setRankInGeography(int rankInGeography) {
            this.rankInGeography = rankInGeography;
        }

        public int getAlreadyPlacedFromGeography() {
            return alreadyPlacedFromGeography;
        }

        public void setAlreadyPlacedFromGeography(int alreadyPlacedFromGeography) {
            this.alreadyPlacedFromGeography = alreadyPlacedFromGeography;
        }
    }
}
