package com.auroratms.tournament;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class Tournament {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
