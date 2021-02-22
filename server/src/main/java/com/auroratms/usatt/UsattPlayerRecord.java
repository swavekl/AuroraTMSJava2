package com.auroratms.usatt;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.util.Date;

// data representing USATT information coming in the ratings file for each tournament
@Data
@NoArgsConstructor
@Entity
@Table(name = "usattplayerrecord")
public class UsattPlayerRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(unique = true)
    private Long membershipId;

    @NonNull
    private Date membershipExpirationDate;

    @NonNull
    @Column(length = 50)
    private String firstName;

    @NonNull
    @Column(length = 50)
    private String lastName;

    private Date dateOfBirth;

    @Column(length = 1)
    private String gender;

    @Column(length = 30)
    private String city;

    @Column(length = 10)
    private String state;

    @Column(length = 25)
    private String zip;

    @Column(length = 60)
    private String country;

    private String homeClub; // home club

    private int tournamentRating;
    private Date lastTournamentPlayedDate;

    private int leagueRating;
    private Date lastLeaguePlayedDate;
}

/*
maxLenFirstName = 26
maxLenLastName  = 30
maxLenState     = 3
maxLenZip       = 18
 */
