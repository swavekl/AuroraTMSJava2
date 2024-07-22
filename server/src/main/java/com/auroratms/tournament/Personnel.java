package com.auroratms.tournament;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Describes a person and his/her role at the tournament: Referee, Umpire or Data entry clerk
 */
@Data
@NoArgsConstructor
public class Personnel implements Serializable {

    // name of the person
    private String name;

    // profile id of a person who is working on the tournament
    private String profileId;

    // a role filled at the tournament
    private String role;
}
