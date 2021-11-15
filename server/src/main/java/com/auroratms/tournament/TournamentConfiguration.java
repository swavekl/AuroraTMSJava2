package com.auroratms.tournament;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class TournamentConfiguration {
    // Late entry date Date to start charging late fees
    private Date lateEntryDate;
    // Entry cutoff date Date to stop accepting on-line entries
    private Date entryCutoffDate;
    // Rating cutoff date Rating cutoff date for event eligibility
    private Date eligibilityDate;
    // Date to stop issuing refunds for withdrawal from event or tournament
    private Date refundDate;
    // url where blank entry form is located
    private String blankEntryUrl;
    // maximum events player can enter per day
    private int maxDailyEvents;
    // maximum number of events a player can enter in a tournament
    private int maxTournamentEvents;
    // number of tables available for play
    private int numberOfTables;
    // type of tournament
    private TournamentType tournamentType;
    // some tournaments have
    private int registrationFee;
    // fee for late entry
    private int lateEntryFee;
    // determines how to calculate total due
    private PricingMethod pricingMethod;
    // list of personnel showing their role at the tournament
    private List<Personnel> personnelList;
    // type of check in for the tournament
    private CheckInType checkInType;
}
