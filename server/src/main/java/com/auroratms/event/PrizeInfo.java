package com.auroratms.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Class holding information about the prize money and/or trophies awarded for each place
 */
@Data
public class PrizeInfo implements Serializable {

    // if draw type is division (e.e. it is a giant round robin event)
    // each group of 8 players designates a division for which this prize info is configured e.g.
    // Division 1 fist place winner gets $150, but division 2 gets only $100
    // for snake draw type this is ignored i.e. 0
    private String division;

    // place for which this prize is awarded
    private int awardedForPlace;

    // if prizes are awarded for range of places e.g. 3 - 4, 5 -8, this will be the range end
    // if it is not a range it will be the same as the awardedForPlace value
    private int awardedForPlaceRangeEnd;

    // prize money awarded or null if nothing
    private Integer prizeMoneyAmount;

    // if true trophy is awarded
    private boolean awardTrophy;

    public PrizeInfo() {
    }

    public PrizeInfo(String division, int awardedForPlace, int awardedForPlaceRangeEnd, Integer prizeMoneyAmount, boolean awardTrophy) {
        this.division = division;
        this.awardedForPlace = awardedForPlace;
        this.awardedForPlaceRangeEnd = awardedForPlaceRangeEnd;
        this.prizeMoneyAmount = prizeMoneyAmount;
        this.awardTrophy = awardTrophy;
    }

    public PrizeInfo(PrizeInfo prizeInfo) {
        this.division = prizeInfo.division;
        this.awardedForPlace = prizeInfo.awardedForPlace;
        this.awardedForPlaceRangeEnd = prizeInfo.awardedForPlaceRangeEnd;
        this.prizeMoneyAmount = prizeInfo.prizeMoneyAmount;
        this.awardTrophy = prizeInfo.awardTrophy;
    }
}
