package com.auroratms.umpire;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Information about an individual match umpired by the umpire or assistant umpire
 */
@Data
@NoArgsConstructor
public class UmpiredMatchInfo implements Serializable {

    // date of the match
    private Date matchDate;

    // tournament name
    private String tournamentName;

    // event name
    private String eventName;

    // final, semifinal or round-robin
    private String roundName;

    // names of players who played the match or doubles team names
    private String playerAName;
    private String playerBName;

    // short version of a match score e.e. 9,9,-8,7
    private String matchScore;

    // if true served as assistant umpire during this match
    private boolean isAssistantUmpire;

}
