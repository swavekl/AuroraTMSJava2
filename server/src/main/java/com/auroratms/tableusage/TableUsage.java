package com.auroratms.tableusage;

import lombok.NonNull;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Class which keeps track of table usage at the tournament
 */
@Entity
public class TableUsage implements Serializable {

    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // tournament to which this table belongs
    @NonNull
    private Long tournamentFk;

    // table number
    private int tableNumber;

    // status
    private TableStatus tableStatus = TableStatus.Free;

    // id of match card assigned to play matches at this table or null
    private Long matchCardFk;

    private Date matchStartTime;

    // completed matches and total matches on the match card
    private byte completedMatches;
    private byte totalMatches;

    // identifier of table or other device used to input scores during live score entry
    // to prevent using 2 devices on the same table
    @NonNull
    @Column(length = 25)
    private String scoreInputDeviceId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTournamentFk() {
        return tournamentFk;
    }

    public void setTournamentFk(long tournamentFk) {
        this.tournamentFk = tournamentFk;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public TableStatus getTableStatus() {
        return tableStatus;
    }

    public void setTableStatus(TableStatus tableStatus) {
        this.tableStatus = tableStatus;
    }

    public long getMatchCardFk() {
        return matchCardFk;
    }

    public void setMatchCardFk(long matchCardFk) {
        this.matchCardFk = matchCardFk;
    }

    public Date getMatchStartTime() {
        return matchStartTime;
    }

    public void setMatchStartTime(Date matchStartTime) {
        this.matchStartTime = matchStartTime;
    }

    public byte getCompletedMatches() {
        return completedMatches;
    }

    public void setCompletedMatches(byte completedMatches) {
        this.completedMatches = completedMatches;
    }

    public byte getTotalMatches() {
        return totalMatches;
    }

    public void setTotalMatches(byte totalMatches) {
        this.totalMatches = totalMatches;
    }

    public String getScoreInputDeviceId() {
        return scoreInputDeviceId;
    }

    public void setScoreInputDeviceId(String scoreInputDeviceId) {
        this.scoreInputDeviceId = scoreInputDeviceId;
    }

    @Override
    public String toString() {
        return "TableUsage{" +
                "id=" + id +
                ", tournamentFk=" + tournamentFk +
                ", tableNumber=" + tableNumber +
                ", tableStatus=" + tableStatus +
                ", matchCardFk=" + matchCardFk +
                ", matchStartTime=" + matchStartTime +
                ", completedMatches=" + completedMatches +
                ", totalMatches=" + totalMatches +
                ", scoreInputDeviceId='" + scoreInputDeviceId + '\'' +
                '}';
    }
}
