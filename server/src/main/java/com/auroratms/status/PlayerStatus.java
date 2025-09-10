package com.auroratms.status;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

/**
 * In order to facilitate check in process for each tournament day or tournament event
 */
@Entity
@Table(name = "playerstatus", indexes = {
        @Index(name = "idx_playerprofileid", columnList = "playerProfileId"),
        @Index(name = "idx_tournamentidtournamentday", columnList = "tournamentId,tournamentDay")
})
@Data
@NoArgsConstructor
public class PlayerStatus {

    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // id of the player to whom this status refers
    @NonNull
    @Column(length = 100)
    private String playerProfileId;

    // tournament id for to which this status refers.  This is to support check-in for the whole tournament
    private long tournamentId;

    // day of tournament to which this status refers.  This is to support daily check-in but not for each event.
    // this should let us catch players who have not shown for the nth day.
    private int tournamentDay;

    // event id to which this status refers.  This is to support check-in for each event
    private long eventId;

    // status code
    private EventStatusCode eventStatusCode;

    // reason for not playing - e.g. injured, change of plans, other
    @Column(length = 50)
    private String reason;

    // estimated arrival time
    private String estimatedArrivalTime;
}
