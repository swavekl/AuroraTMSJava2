package com.auroratms.profile;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class UserProfile implements Serializable {
    // Okta profile id
    @Id
    private String userId;
    // login is the initial email they signed up with
    private String login;
    // USATT membership id
    private Long membershipId;

    private String firstName;
    private String lastName;
    private String mobilePhone;
    private String email;
    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private String countryCode;
    private String gender;
    private Date dateOfBirth;
    private Date membershipExpirationDate;
    private Integer tournamentRating;
    private Long homeClubId;
    private String homeClubName;
    // region of regional coordinator
    private String division;
    // active, locked etc.
    private String userStatus;
}
