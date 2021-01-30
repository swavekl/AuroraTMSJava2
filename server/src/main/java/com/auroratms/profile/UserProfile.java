package com.auroratms.profile;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import java.util.Date;

@Data
@NoArgsConstructor
public class UserProfile {
    @Id
    private String userId;

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

}
