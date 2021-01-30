package com.auroratms.tournamententry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "tournamententry")
@NoArgsConstructor
@Getter
@Setter
public class TournamentEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long tournamentFk;

    // date user entered the tournament
    private Date dateEntered;

    // rating as of the Tournament.ratingCutoffDate date  it may go up after that date but will not effect eligibility for entered events
    // can be null i.e. unrated
    int eligibilityRating;

    // current rating used for seeding within events
    int seedRating;

    // selected USATT membership option (from 1 through 8)
    int membershipOption;

    // profile id of the player who owns this entry
    String profileId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TournamentEntry that = (TournamentEntry) o;
        return tournamentFk == that.tournamentFk &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tournamentFk);
    }
}
