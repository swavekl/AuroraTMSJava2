package com.auroratms.match;

import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEventConfiguration;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "matchcard", indexes = {
        @Index(name = "idx_eventfk", columnList = "eventFk")
})
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

    // for single elimination match cards this is the prior round (i.e. round robin) group number from which this player came
    private int playerAGroupNum;
    private int playerBGroupNum;

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

    // String representing player rankings - map of profileId to rank
    @Column(length = 400)
    private String playerRankings;

    // status indicating if score entry can proceed or should be stopped
    private MatchCardStatus status;

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
                ", status=" + status +
                ", playerRankings='" + playerRankings + '\'' +
                ", profileIdToNameMap=" + profileIdToNameMap +
                ", matches=" + matches +
                '}';
    }

    public String getRoundName() {
        String strRound = "";
        switch (round) {
            case 0:
                strRound = "Round Robin";
                break;
            case 2:
                strRound = (groupNum == 1) ? "Final" : "3rd & 4th Place";
                break;
            case 4:
                strRound = "Semi-Final";
                break;
            case 8:
                strRound = "Quarter-Final";
                break;
            default:
                strRound = "Round of " + round;
                break;
        }
        return strRound;
    }

    /**
     * Gets player rankings as map
     * @return
     */
    public Map<Integer, String> getPlayerRankingsAsMap() {
        Map<Integer, String> rankToProfileIdMap = new HashMap<>();
        try {
            if (playerRankings != null) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> map = mapper.readValue(playerRankings, Map.class);
                for (String rank : map.keySet()) {
                    String profileId = map.get(rank);
                    Integer iRank = Integer.valueOf(rank);
                    rankToProfileIdMap.put(iRank, profileId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rankToProfileIdMap;
    }

}
