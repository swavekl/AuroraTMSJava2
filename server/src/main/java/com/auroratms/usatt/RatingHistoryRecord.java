package com.auroratms.usatt;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "usattratinghistory",
        uniqueConstraints = {
                @UniqueConstraint(name = "idx_member_rating_date", columnNames = {"membershipId", "initialRatingDate", "finalRatingDate"})
        }
)
public class RatingHistoryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // usatt membership id
    @Column(nullable = false)
    private long membershipId;

    // tournament start date
    @NonNull
    @Column(nullable = false)
    private Date initialRatingDate;

    // tournament end date
    @NonNull
    @Column(nullable = false)
    private Date finalRatingDate;

    // rating at the start of the tournament
    @Column(nullable = false)
    private int initialRating;

    // rating after the tournament
    @Column(nullable = false)
    private int finalRating;

    // foreign key of tournament which changed this rating
    @Column()
    private Long tournamentFk;
}
