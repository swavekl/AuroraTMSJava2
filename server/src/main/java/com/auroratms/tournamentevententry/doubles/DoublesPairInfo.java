package com.auroratms.tournamentevententry.doubles;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Combines information of doubles entry and profile informaiton for both players
 */
@Data
@NoArgsConstructor
public class DoublesPairInfo {

    private long id;

    private DoublesPair doublesPair;

    private String playerAProfileId;
    private String playerBProfileId;

    private String playerAName;
    private String playerBName;

    private int playerAEligibilityRating;
    private int playerBEligibilityRating;

    private int playerASeedRating;
    private int playerBSeedRating;
}
