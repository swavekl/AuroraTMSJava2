package com.auroratms.match;

import com.auroratms.draw.DrawType;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "matchcard")
@Data
@NoArgsConstructor
public class MatchCard {
    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // event id to which this match belongs
    private long eventFk;

    // group number. if this is a card for group of matches it is 1, 2, 3 etc.
    // for single elimination phase it will be 0
    private int groupNum;

    // table numbers assigned to this match card could be one e.g. table number 4
    // or multiple if this is round robin phase 13,14
    @Column(length = 30)
    private String assignedTables;

    // list of matches for this match card
    @OneToMany(mappedBy = "matchCard", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Match> matches;

    // match for draw type
    private DrawType drawType;

    // best of 3, 5, 7 or 9 games per match in the main round (i.e. round robin)
    private int numberOfGames;
}
