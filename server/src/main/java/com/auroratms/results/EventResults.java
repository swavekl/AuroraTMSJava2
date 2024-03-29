package com.auroratms.results;

import lombok.Data;

import java.util.List;

@Data
public class EventResults {

    // is single elimination round result
    private boolean singleElimination;

    // if true this is doubles event
    private boolean doubles;

    // round robin group number
    private int groupNumber;

    // round of 16, 8 , quarterfinals etc.
    private int round;

    private List<PlayerResults> playerResultsList;
}
