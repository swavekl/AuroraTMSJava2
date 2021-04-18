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

    // type of entry - most of the time individual but may be also family
    EntryType entryType = EntryType.INDIVIDUAL;

    // if family or group entry, will contain owning entry fk
    Long owningTournamentEntryFk;

    // mandatory usattDonation line - optional for user
    int usattDonation;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TournamentEntry that = (TournamentEntry) o;
        return tournamentFk == that.tournamentFk && eligibilityRating == that.eligibilityRating && seedRating == that.seedRating && membershipOption == that.membershipOption && id.equals(that.id) && dateEntered.equals(that.dateEntered) && profileId.equals(that.profileId) && entryType == that.entryType && Objects.equals(owningTournamentEntryFk, that.owningTournamentEntryFk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tournamentFk, dateEntered, eligibilityRating, seedRating, membershipOption, profileId, entryType, owningTournamentEntryFk);
    }
}
