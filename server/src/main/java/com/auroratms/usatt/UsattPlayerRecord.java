package com.auroratms.usatt;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usattplayerrecord", indexes = {
        @Index(name = "idx_membershipid", columnList = "membershipId"),
        @Index(name = "idx_usatt_lastname", columnList = "lastName"),
        @Index(name = "idx_usatt_lastname_firstname", columnList = "lastName, firstName"),
        @Index(name = "idx_usatt_guid", columnList = "memberGuid")
})
public class UsattPlayerRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @CsvBindByName(column = "USATT Id")
    @Column(unique = true, nullable = false)
    private Long membershipId;

    @CsvBindByName(column = "Latest Membership expiry date")
    @CsvDate("MM/dd/yyyy") // Update this format to match whatever is exactly in your CSV
    private Date membershipExpirationDate;

    @NonNull
    @CsvBindByName(column = "First Name")
    @Column(length = 50, nullable = false)
    private String firstName;

    @NonNull
    @CsvBindByName(column = "Last Name")
    @Column(length = 50, nullable = false)
    private String lastName;

    @CsvBindByName(column = "Date Of Birth")
    @CsvDate("MM/dd/yyyy")
    private Date dateOfBirth;

    @CsvBindByName(column = "Gender")
    @Column(length = 1)
    private String gender;

    @CsvBindByName(column = "CityTown")
    @Column(length = 30)
    private String city;

    @CsvBindByName(column = "State")
    @Column(length = 10)
    private String state;

    @CsvBindByName(column = "ZipCode")
    @Column(length = 25)
    private String zip;

    @CsvBindByName(column = "Country") // If not present in CSV, OpenCSV safely ignores it
    @Column(length = 60)
    private String country;

    @CsvBindByName(column = "Primary Club")
    private String homeClub;

    @CsvBindByName(column = "FinalRating")
    private int tournamentRating;

    @CsvBindByName(column = "Latest tournament play date")
    @CsvDate("MM/dd/yyyy")
    private Date lastTournamentPlayedDate;

    @CsvBindByName(column = "League Rating")
    private int leagueRating;

    @CsvBindByName(column = "Rating As Of Date")
    @CsvDate("MM/dd/yyyy")
    private Date lastLeaguePlayedDate;

    @CsvBindByName(column = "Latest Membership")
    private String membershipType;

    // Inside UsattPlayerRecord class
    @Column(length = 50)
    private String memberGuid;

    // Added fields that are in your CSV but weren't in your original Entity
    @Transient // Marked as Transient if you don't want to save these to the database
    @CsvBindByName(column = "Middle Name")
    private String middleName;

    @Transient
    @CsvBindByName(column = "Nick Name")
    private String nickName;

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
