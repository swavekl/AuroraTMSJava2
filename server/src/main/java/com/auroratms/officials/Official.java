package com.auroratms.officials;

import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Match official i.e. Referee or Umpire or both
 */
@Entity
@Table(name = "official")
@NoArgsConstructor
public class Official {

    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String firstName;
    private String lastName;

    // Okta profile id
    private String profileId;

    // umpire and referee rank of this official
    private UmpireRank umpireRank;
    @Column(nullable = true)
    private RefereeRank refereeRank;

    // umpire and referee number - international ?
    @Column(nullable = true)
    private long umpireNumber;
    @Column(nullable = true)
    private long refereeNumber;

    // wheelchair certification ? e.g. IPTTC or number
    @Column(nullable = true)
    private String wheelchair;

    // USATT membership id of this official, fetched from userprofileext
    @Transient
    private long membershipId;

    // state of residence
    private String state;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public UmpireRank getUmpireRank() {
        return umpireRank;
    }

    public void setUmpireRank(UmpireRank umpireRank) {
        this.umpireRank = umpireRank;
    }

    public RefereeRank getRefereeRank() {
        return refereeRank;
    }

    public void setRefereeRank(RefereeRank refereeRank) {
        this.refereeRank = refereeRank;
    }

    public long getUmpireNumber() {
        return umpireNumber;
    }

    public void setUmpireNumber(long umpireNumber) {
        this.umpireNumber = umpireNumber;
    }

    public long getRefereeNumber() {
        return refereeNumber;
    }

    public void setRefereeNumber(long refereeNumber) {
        this.refereeNumber = refereeNumber;
    }

    public String getWheelchair() {
        return wheelchair;
    }

    public void setWheelchair(String wheelchair) {
        this.wheelchair = wheelchair;
    }

    public long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(long membershipId) {
        this.membershipId = membershipId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
