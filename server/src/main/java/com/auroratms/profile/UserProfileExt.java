package com.auroratms.profile;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

/**
 * Maps Okta profile id to USATT membership id - we can store other information here to
 * make it easier to combine it with other data rather than getting it via REST api from Okta
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "userprofileext")
public class UserProfileExt {

    // Okta profile id
    @Id
    @NonNull
    @Column(unique = true, nullable = false, length = 50)
    private String profileId;

    // USATT membership id - foreign key into usatt record
    @NonNull
    @Column(unique = true, nullable = false)
    private Long membershipId;

    // foreign key to club's table
    private Long clubFk;
}
