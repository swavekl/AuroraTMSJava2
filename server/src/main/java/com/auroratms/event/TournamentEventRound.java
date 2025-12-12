package com.auroratms.event;

import com.auroratms.draw.DrawType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Information about each round used to generate draws
 */
@NoArgsConstructor
@Getter
@Setter
public class TournamentEventRound implements Serializable {
    // e.g. Preliminary RR, Qualifying RR, Championship
    private String roundName;

    // round ordinal number so we can identify draws for this round 1, 2, 3
    private int ordinalNum;

    // single elimination or round robin
    private boolean singleElimination;

    // for multi day tournaments the round may start the next day
    private int day;

    // start time for this round
    private double startTime;

    // definitions of each division i.e. Championship, Class AA, Class A, B, C, D
    private List<TournamentEventRoundDivision> divisions;

    public TournamentEventRound(TournamentEventRound round) {
        this.roundName = round.roundName;
        this.ordinalNum = round.ordinalNum;
        this.singleElimination = round.singleElimination;
        this.startTime = round.startTime;
        this.divisions = new ArrayList<>(round.getDivisions().size());
        for (TournamentEventRoundDivision division : round.getDivisions()) {
            TournamentEventRoundDivision copy = new TournamentEventRoundDivision(division);
            divisions.add(copy);
        }
    }
}
