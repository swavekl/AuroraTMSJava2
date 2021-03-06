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

    private String clubName;

    // address
    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private String countryCode;

}
