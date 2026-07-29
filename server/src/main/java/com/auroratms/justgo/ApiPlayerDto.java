package com.auroratms.justgo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiPlayerDto {
    private String memberNumber;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String dob;            // "1960-11-24"
    private String gender;
    private String address1;
    private String address2;
    private String town;           // Maps to CityTown
    private String county;         // Maps to State
    private String postCode;       // Maps to ZipCode
    private String country;
    private String phoneNumber;
    private String memberStatus;   // Maps to Latest Membership
    private String registerDate;
    private String id;             // GUID
    private String tournamentRating;
    private String membershipType;
    private String membershipExpirationDate;
    private String clubName;
}
