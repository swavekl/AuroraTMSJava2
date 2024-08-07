package com.auroratms.clubaffiliationapp;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents an alternative club playing location and when it is open
 */
@Data
@NoArgsConstructor
public class PlayingSite implements Serializable {
    private String buildingName;
    // playing site address
    private String streetAddress;
    private String city;
    private String state;
    // 5 or Zip+4 code
    private String zipCode;

    private String hoursAndDates;
}

