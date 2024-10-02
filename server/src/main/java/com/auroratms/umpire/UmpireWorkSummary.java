package com.auroratms.umpire;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Summary of work by umpire during the tournament
 */
@Data
@NoArgsConstructor
public class UmpireWorkSummary implements Serializable {

    // umpire profileid
    private String profileId;

    // name of the umpire
    private String umpireName;

    // indicates if umpire is officiating a match currently
    private boolean isBusy;

    // number of matches umpired as a main umpire during this tournament
    private int numUmpiredMatches;

    // number of matches umpired as an assistant umpire during this tournament
    private int numAssistantUmpiredMatches;
}
