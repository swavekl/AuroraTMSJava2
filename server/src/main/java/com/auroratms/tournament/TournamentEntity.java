package com.auroratms.tournament;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.util.Date;

/**
 * This tournament object IS persisted.
 */
@Entity
@Table(name = "tournament")
@Data
@NoArgsConstructor
public class TournamentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(length = 60)
    private String name;

    // venue information
    @Column(length = 100)
    private String venueName;
    private String streetAddress;
    @Column(length = 100)
    private String city;
    @Column(length = 40)
    private String state;
    @Column(length = 20)
    private String zipCode;
    private Date startDate;
    private Date endDate;
    private int starLevel;

    // tournament director contact information
    @Column(length = 60)
    private String contactName;
    @Column(length = 60)
    private String email;
    @Column(length = 30)
    private String phone;

    // to avoid having to change database schema each time we add new field to configuration
    // we will persist configuration as JSON in this field.
    @Column(length = 6000)
    private String content;

}
