package com.auroratms.match;

import com.auroratms.draw.DrawType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "matchcard")
@Data
@NoArgsConstructor
public class MatchCard implements Serializable {
    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // event id to which this match belongs
    private long eventFk;

    // group number e.g. 1, 2, 3 etc.
    private int groupNum;

    // table numbers assigned to this match card could be one e.g. table number 4
    // or multiple if this is round robin phase 13,14
    @Column(length = 30)
    private String assignedTables;

    // list of matches for this match card
    @OneToMany(mappedBy = "matchCard", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Match> matches;

    // match for draw type
    private DrawType drawType;

    // best of 3, 5, 7 or 9 games per match in the main round (i.e. round robin)
    private int numberOfGames;

    // for round robin phase 0,
    // for single elimination - 64, 32, 16, 8 (quarter finals), 4 (semifinals), 2 (finals and 3rd/4th place)
    private int round;

    // day of the tournament on which this event is played 1, 2, 3 etc
    private int day;

    // fractional start time e.g. 9.5 = 9:30 am, 17.0 = 5:00 pm, -1.0 = To be Determined
    private double startTime;

    // total scheduled duration in minutes of all matches on this match card.
    // This is assuming that they will be played on 1 table only, if played on 2 tables divide that by 2, if on 3 dividde by
    // so if played on 2 tables it will
    private int duration;

    // String representing player rankings
    @Column(length = 400)
    private String playerRankings;

//    // names of umpire and assistant umpire if match is umpired
//    @Column(length = 50)
//    private String umpireName;
//
//    @Column(length = 50)
//    private String assistantUmpireName;

    // map of player profile ids to their names
    @Transient
    Map<String, String> profileIdToNameMap;

    @Override
    public String toString() {
        return "MatchCard{" +
                "id=" + id +
                ", eventFk=" + eventFk +
                ", drawType=" + drawType +
                ", round=" + round +
                ", groupNum=" + groupNum +
                ", assignedTables='" + assignedTables + '\'' +
                ", numberOfGames=" + numberOfGames +
                ", day=" + day +
                ", startTime=" + startTime +
                ", duration=" + duration +
                ", playerRankings='" + playerRankings + '\'' +
//                ", umpireName='" + umpireName + '\'' +
//                ", assistantUmpireName='" + assistantUmpireName + '\'' +
                ", profileIdToNameMap=" + profileIdToNameMap +
                ", matches=" + matches +
                '}';
    }
}
