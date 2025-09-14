package com.auroratms.umpire;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Umpire work assignment - created so that it is more efficient to retrieve
 * a list of matches umpired by an individual umpire
 */
@Entity
@Table(name = "umpirework")
@Data
@NoArgsConstructor
public class UmpireWork implements Serializable {

    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // id of a match which is umpired
    private long matchFk;

    // event fk so it is easy to get event name
    private long eventFk;

    private long tournamentFk;

    // profile id of the umpire plus assistant umpire if any separated by ;
    @NonNull
    @Column(length = 50)
    private String umpireProfileId;

    // profile id of the assistant umpire if any separated by ;
    @Column(length = 50)
    private String assistantUmpireProfileId;

    // the date and time when the match was assigned
    // i.e. approximately when the match was actually played
    @NonNull
    private Date matchDate;
}
