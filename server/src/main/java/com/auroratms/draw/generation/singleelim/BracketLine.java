package com.auroratms.draw.generation.singleelim;

import lombok.Data;

/**
 * Helper class for making the draw and keeping information about lines of the bracket
 */
@Data
public class BracketLine {
    // line number e.g. 1 for top seeded player,
    // 2 for second top seeded player,
    // 3 for 2 players seeded 3 & 4,
    // 5 for 4 players seeded 5, 6, 7, & 8
    // 9 for 8 for players seeded 9 - 16,
    // 17 for 16 players seeded 17 - 32 etc. in powers of 2 number of players
    int normalizedSeedNumber;

    // order in which this line number was placed into draw line
    // e.g, 3 or 4 for 3s
    // 5, 6, 7 or 8 for 5s
    int seedNumber;

    // 0 if not a bye, or positive number - 1 means bye for player seeded number 1, 2 for player seeded #2 etc
    int byeSeedNumber = 0;

    boolean isBye;

    public BracketLine(int normalizedSeedNumber, int seedNumber, boolean isBye) {
        this.normalizedSeedNumber = normalizedSeedNumber;
        this.seedNumber = seedNumber;
        this.isBye = isBye;
    }
}
