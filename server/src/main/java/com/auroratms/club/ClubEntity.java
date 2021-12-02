package com.auroratms.club;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Represents table tennis club (affiliated with USATT and perhaps non-affiliated too)
 */
@Entity
@Table(name = "club")
@Data
@NoArgsConstructor
public class ClubEntity {

    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // full name of the club
    private String clubName;

    // alternative names and acronym, comma separated
    private String alternateClubNames;

    // e.g Eola Community Center
    private String buildingName;

    // address
    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private String countryCode;

    // if true this is an affiliated club
    private boolean isAffiliated;

    // Wednesday & Friday - 6:30 - 9:30PM
    private String hoursAndDates;

    private String clubAdminName;
    private String clubAdminEmail;

    private String clubPhoneNumber;
    private String clubPhoneNumber2;

    private String clubWebsite;
}
