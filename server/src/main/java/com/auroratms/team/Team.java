package com.auroratms.team;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
//@Table(name = "team", indexes = {
//        @Index(name = "idx_teamfk", columnList = "tournamentFk")
//})
@NoArgsConstructor
@Getter
@Setter
public class Team implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // teams event which this is team entered
    @Column(nullable = false)
    private long tournamentEventFk;

    // team name
    @Column(length = 35)
    private String name;

    // team rating
    private int rating;

    // indicates the team captain's profile id
    @Column(nullable = false)
    private String captainProfileId;


}
