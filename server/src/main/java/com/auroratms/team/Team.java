package com.auroratms.team;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "team", indexes = {
        @Index(name = "idx_tournament_event", columnList = "tournamentEventFk")
})
@NoArgsConstructor
@Getter
@Setter
public class Team implements Serializable, Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // teams event which this is team entered
    @Column(nullable = false)
    private long tournamentEventFk;

    // team name
    @Column(length = 35)
    private String name;

    // team rating
    private int teamRating;

    // date of creation
    private Date createdDate;

    // price of entry since it may change over time
    private double entryPricePaid;

    // list of team members of this team
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Tells Jackson to serialize the list of members
    private List<TeamMember> teamMembers = new java.util.ArrayList<>();

    @Transient
    private String cartSessionId;

    @Override
    public Team clone() {
        try {
            Team clone = (Team) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    // In Team.java
    public void addTeamMember(TeamMember member) {
        this.teamMembers.add(member);
        member.setTeam(this); // This is the line that fixed your save!
    }

    public void removeTeamMember(TeamMember member) {
        this.teamMembers.remove(member);
        member.setTeam(null);
    }
}
