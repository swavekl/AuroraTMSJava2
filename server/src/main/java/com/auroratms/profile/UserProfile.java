package com.auroratms.profile;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
public class UserProfile {
    @Id
    private String userId;

    private String firstName;
    private String lastName;
    private String mobilePhone;
    private String email;
    private String city;
    private String state;
    private String zipCode;

    public UserProfile(String userId, Map<String, Object> oktaUserProfile) {
        this.userId = userId;
        this.firstName = (String) oktaUserProfile.get("firstName");
        this.lastName = (String) oktaUserProfile.get("lastName");
        this.mobilePhone = (String) oktaUserProfile.get("mobilePhone");
        this.email = (String) oktaUserProfile.get("email");
        this.city = (String) oktaUserProfile.get("city");
        this.state = (String) oktaUserProfile.get("state");
        this.zipCode = (String) oktaUserProfile.get("zipCode");
    }
}
