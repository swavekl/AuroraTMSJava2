package com.auroratms.draw.generation.singleelim;

import com.auroratms.draw.generation.PlayerDrawInfo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeographicDistanceCalculatorTest {

    @Test
    public void testClub () {
        GeographicalDistanceCalculator calculator = new GeographicalDistanceCalculator();
        {
            PlayerDrawInfo playerDrawInfo1 = makePlayerDrawInfo("Muhammad, James", "Farmington Hills TTC", null, "MI", 1956);
            PlayerDrawInfo playerDrawInfo2 = makePlayerDrawInfo("Boghikian, Razmig", "Farmington Hills TTC", null, "", 1896);
            int distance = calculator.getDistance(playerDrawInfo1, playerDrawInfo2);
            assertEquals("same club should be closest distance", 1, distance);
        }

        {
            PlayerDrawInfo playerDrawInfo3 = makePlayerDrawInfo("Loganathan, Aarthi", "Table Tennis Minnesota", null, "MN", 1907);
            PlayerDrawInfo playerDrawInfo4 = makePlayerDrawInfo("Imbo, Sam Oluoch", "MN TTC", null, "MN", 1957);
            int distance = calculator.getDistance(playerDrawInfo3, playerDrawInfo4);
            assertEquals("different club should be larger distance", 3, distance);
        }
    }

    @Test
    public void testCity () {
        GeographicalDistanceCalculator calculator = new GeographicalDistanceCalculator();

        {
            PlayerDrawInfo playerDrawInfo1 = makePlayerDrawInfo("Player A", "TT Club1", "Detroit", "MI", 1956);
            PlayerDrawInfo playerDrawInfo2 = makePlayerDrawInfo("Player B", "TT Club2", "Detroit", "MI", 1896);
            int distance = calculator.getDistance(playerDrawInfo1, playerDrawInfo2);
            assertEquals("same city should distance should be", 2, distance);
        }

        {
            PlayerDrawInfo playerDrawInfo1 = makePlayerDrawInfo("Player A", "TT Club1", "Detroit", "MI", 1956);
            PlayerDrawInfo playerDrawInfo2 = makePlayerDrawInfo("Player B", "TT Club2", "Grand Rapids", "MI", 1896);
            int distance = calculator.getDistance(playerDrawInfo1, playerDrawInfo2);
            assertEquals("different city in the same state", 3, distance);
        }

        {
            PlayerDrawInfo playerDrawInfo1 = makePlayerDrawInfo("Player A", "TT Club1", "Kansas City", "KS", 1956);
            PlayerDrawInfo playerDrawInfo2 = makePlayerDrawInfo("Player B", "TT Club2", "Kansas City", "MO", 1896);
            int distance = calculator.getDistance(playerDrawInfo1, playerDrawInfo2);
            assertEquals("city with same name in different state", 4, distance);
        }
    }

    @Test
    public void testState () {
        GeographicalDistanceCalculator calculator = new GeographicalDistanceCalculator();

        {
            PlayerDrawInfo playerDrawInfo1 = makePlayerDrawInfo("Player A", "TT Club1", null, "MI", 1956);
            PlayerDrawInfo playerDrawInfo2 = makePlayerDrawInfo("Player B", "TT Club2", null, "MI", 1896);
            int distance = calculator.getDistance(playerDrawInfo1, playerDrawInfo2);
            assertEquals("same city should distance should be", 3, distance);
        }

        {
            PlayerDrawInfo playerDrawInfo1 = makePlayerDrawInfo("Player A", "TT Club1", null, "MI", 1956);
            PlayerDrawInfo playerDrawInfo2 = makePlayerDrawInfo("Player B", "TT Club2", null, "IL", 1896);
            int distance = calculator.getDistance(playerDrawInfo1, playerDrawInfo2);
            assertEquals("same city should distance should be", 4, distance);
        }
    }

    @Test
    public void testCountry () {
        GeographicalDistanceCalculator calculator = new GeographicalDistanceCalculator();

        {
            PlayerDrawInfo playerDrawInfo1 = makePlayerDrawInfo("Player A", "TT Club1", null, null, 1956);
            PlayerDrawInfo playerDrawInfo2 = makePlayerDrawInfo("Player B", "TT Club2", null, null, 1896);
            int distance = calculator.getDistance(playerDrawInfo1, playerDrawInfo2);
            assertEquals("same city should distance should be", 4, distance);
        }

        {
            PlayerDrawInfo playerDrawInfo1 = makePlayerDrawInfo("Player A", "TT Club1", null, null, 1956);
            playerDrawInfo1.setCountry("MX");
            PlayerDrawInfo playerDrawInfo2 = makePlayerDrawInfo("Player B", "TT Club2", null, null, 1896);
            playerDrawInfo2.setCountry("CA");
            int distance = calculator.getDistance(playerDrawInfo1, playerDrawInfo2);
            assertEquals("same city should distance should be", 5, distance);
        }
    }

    private PlayerDrawInfo makePlayerDrawInfo(String fullName, String clubName, String city, String state, int rating) {
        PlayerDrawInfo playerDrawInfo = new PlayerDrawInfo();
        playerDrawInfo.setPlayerName(fullName);
        playerDrawInfo.setClubName(clubName);
        playerDrawInfo.setCity(city);
        playerDrawInfo.setState(state);
        playerDrawInfo.setRating(rating);
        return playerDrawInfo;
    }

}
