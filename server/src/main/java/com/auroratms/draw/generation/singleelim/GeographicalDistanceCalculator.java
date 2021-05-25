package com.auroratms.draw.generation.singleelim;

import com.auroratms.draw.generation.PlayerDrawInfo;

/**
 * Function for calculating geographical distance
 * which takes into consideration if they are from the same club,
 * same city, same state or country.
 * If they are from the same club means they play each other often so should be separated the farthest e.g. distance = 1
 * from the same city in the same state (e.g.
 */
public class GeographicalDistanceCalculator {

    /**
     *
     * @param playerDrawInfo1
     * @param playerDrawInfo2
     * @return
     */
    public int getDistance (PlayerDrawInfo playerDrawInfo1, PlayerDrawInfo playerDrawInfo2) {
        String p1Club = playerDrawInfo1.getClubName();
        String p2Club = playerDrawInfo2.getClubName();
        // check if they are from the same club - that's the closest proximity
        if ((p1Club != null && p2Club != null) && p1Club.equals(p2Club)) {
            return 1;
        } else {
            return checkIfFromSameCity(playerDrawInfo1, playerDrawInfo2);
        }
    }

    /**
     *
     * @param playerDrawInfo1
     * @param playerDrawInfo2
     * @return
     */
    private int checkIfFromSameCity(PlayerDrawInfo playerDrawInfo1, PlayerDrawInfo playerDrawInfo2) {
        String p1City = playerDrawInfo1.getCity();
        String p2City = playerDrawInfo2.getCity();
        // check if from the same city
        if ((p1City != null && p2City != null) && p1City.equals(p2City)) {
            String p1State = playerDrawInfo1.getState();
            String p2State = playerDrawInfo2.getState();
            // in USA there are cities with the same name in multiple states
            if ((p1State != null && p2State != null) && p1State.equals(p2State)) {
                return 2;
            } else {
                // city name is the same but city is in a different state - so it is not the same
                return checkIfFromSameState(playerDrawInfo1, playerDrawInfo2);
            }
        } else {
            return checkIfFromSameState(playerDrawInfo1, playerDrawInfo2);
        }
    }

    private int checkIfFromSameState(PlayerDrawInfo playerDrawInfo1, PlayerDrawInfo playerDrawInfo2) {
        String p1State = playerDrawInfo1.getState();
        String p2State = playerDrawInfo2.getState();
        // in USA there are cities with the same name in multiple states
        if ((p1State != null && p2State != null) && p1State.equals(p2State)) {
            return 3;
        } else {
            return checkIfFromSameCountry (playerDrawInfo1, playerDrawInfo2);
        }
    }

    private int checkIfFromSameCountry(PlayerDrawInfo playerDrawInfo1, PlayerDrawInfo playerDrawInfo2) {
        String p1Country = playerDrawInfo1.getCountry();
        String p2Country = playerDrawInfo2.getCountry();
        if ((p1Country != null && p2Country != null) && p1Country.equals(p2Country)) {
            return 4;
        } else {
            return 5; // furthest separation
        }
    }
}
