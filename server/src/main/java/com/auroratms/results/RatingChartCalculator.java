package com.auroratms.results;

public class RatingChartCalculator {

    private static final RatingChartEntry [] ratingsChartEntries = {
            new RatingChartEntry(0, 12, 8, 8),
            new RatingChartEntry(13, 37, 7, 10),
            new RatingChartEntry(38, 62, 6, 13),
            new RatingChartEntry(63, 87, 5,16),
            new RatingChartEntry(88, 112, 4,20),
            new RatingChartEntry(113, 137, 3,25),
            new RatingChartEntry(138, 162, 2,30),
            new RatingChartEntry(163, 187, 2,35),
            new RatingChartEntry(188, 212, 1,40),
            new RatingChartEntry(213, 237, 1,45),
            new RatingChartEntry(238, 3000, 0,50)
    };

    public static int getExchangedPoints(int playerARating, int playerBRating, boolean playerAWonMatch) {
        int exchangedPoints = 0;
        int ratingPointsDifference = (playerARating > playerBRating) ? (playerARating - playerBRating) :
                (playerBRating - playerARating);
        boolean expectedResult = ((playerARating > playerBRating) && playerAWonMatch) ||
                ((playerARating < playerBRating) && !playerAWonMatch);
        for (RatingChartEntry ratingsChartEntry : ratingsChartEntries) {
            if (ratingPointsDifference >= ratingsChartEntry.lowerBoundPointSpread &&
                ratingPointsDifference <= ratingsChartEntry.upperBoundPointSpread) {
                exchangedPoints = (expectedResult)
                        ? ratingsChartEntry.expectedResultPointsExchanged
                        : ratingsChartEntry.upsetResultPointsExchanged;
                exchangedPoints = exchangedPoints * (playerAWonMatch ? 1 : -1);
                break;
            }
        }
        return exchangedPoints;
    }


    private static class RatingChartEntry {
        int lowerBoundPointSpread;
        int upperBoundPointSpread;
        int expectedResultPointsExchanged;
        int upsetResultPointsExchanged;

        public RatingChartEntry(int lowerBoundPointSpread, int upperBoundPointSpread, int expectedResultPointsExchanged, int upsetResultPointsExchanged) {
            this.lowerBoundPointSpread = lowerBoundPointSpread;
            this.upperBoundPointSpread = upperBoundPointSpread;
            this.expectedResultPointsExchanged = expectedResultPointsExchanged;
            this.upsetResultPointsExchanged = upsetResultPointsExchanged;
        }
    }
}
