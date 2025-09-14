package com.auroratms.usatt;

import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Objects;

// data representing USATT information coming in the ratings file for each tournament
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usattplayerrecord", indexes = {
        @Index(name = "idx_membershipid", columnList = "membershipId")
})
public class UsattPlayerRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(unique = true, nullable = false)
    private Long membershipId;

    // for foreign players we don't have USATT membership expiration.
    // It is controlled by their country's association expiration date
    private Date membershipExpirationDate;

    @NonNull
    @Column(length = 50, nullable = false)
    private String firstName;

    @NonNull
    @Column(length = 50, nullable = false)
    private String lastName;

    private Date dateOfBirth;

    @Column(length = 1)
    private String gender;

    @Column(length = 30)
    private String city;

    @Column(length = 10)
    private String state;

    @Column(length = 25)
    private String zip;

    @Column(length = 60)
    private String country;

    private String homeClub; // home club

    private int tournamentRating;
    private Date lastTournamentPlayedDate;

    private int leagueRating;
    private Date lastLeaguePlayedDate;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UsattPlayerRecord that = (UsattPlayerRecord) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}

