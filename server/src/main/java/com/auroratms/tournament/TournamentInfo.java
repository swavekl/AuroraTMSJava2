package com.auroratms.tournament;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * Smaller information used only for viewing / registering for tournaments, where not all tournaemnt
 * configuration information is needed
 */
@Data
@NoArgsConstructor
public class TournamentInfo {
    private Long id;

    private @NonNull String name;
    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private Date startDate;
    private Date endDate;
    private int starLevel;
}
