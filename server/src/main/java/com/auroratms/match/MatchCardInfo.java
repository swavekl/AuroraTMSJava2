package com.auroratms.match;

import com.auroratms.draw.DrawType;
import lombok.Data;

/**
 * Abbreviated match card information - without matches
 */
@Data
public class MatchCardInfo {

    private long id;

    // match for draw type
    private DrawType drawType;

    // for round robin phase 0,
    // for single elimination - 64, 32, 16, 8 (quarter finals), 4 (semifinals), 2 (finals and 3rd/4th place)
    private int round;

    // group number e.g. 1, 2, 3 etc.
    private int groupNum;

    // table numbers assigned to this match card could be one e.g. table number 4
    // or multiple if this is round robin phase 13,14
    private String assignedTables;

    // fractional start time e.g. 9.5 = 9:30 am, 17.0 = 5:00 pm, -1.0 = To be Determined
    private double startTime;

    public MatchCardInfo(long id, DrawType drawType, int round, int groupNum, String assignedTables, double startTime) {
        this.id = id;
        this.drawType = drawType;
        this.round = round;
        this.groupNum = groupNum;
        this.assignedTables = assignedTables;
        this.startTime = startTime;
    }
}
