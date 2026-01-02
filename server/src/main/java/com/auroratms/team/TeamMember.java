package com.auroratms.team;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "team_member", indexes = {
        // Fast lookup for "Find all teams this player belongs to"
        @Index(name = "idx_member_profile", columnList = "profile_id"),
        // Fast lookup for "Find a specific player within a specific team"
        @Index(name = "idx_member_team_profile", columnList = "team_fk, profile_id"),
        // Speeds up checking who the captain is for a team
        @Index(name = "idx_member_captain", columnList = "team_fk, is_captain")
})
@NoArgsConstructor
@Getter
@Setter
public class TeamMember implements Serializable, Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_fk")
    @JsonBackReference // Prevents TeamMember from re-serializing the parent Team
    private Team team;

    @Column(name = "profile_id")
    private String profileId;

    @Column(name = "is_captain")
    private boolean isCaptain;

    @Enumerated(EnumType.STRING)
    private TeamEntryStatus status;

    // The link to the actual registration/entry details
    @Column(name = "tournament_entry_fk")
    private Long tournamentEntryFk;

    @Column(name = "tournament_event_fk", nullable = false)
    private Long tournamentEventFk;

    // session id for deleting
    @Column(length = 36)
    private String cartSessionId;

    // name of the player
    @Transient
    private String playerName;

    // player rating
    @Transient
    private int playerRating;

    @Override
    public TeamMember clone() {
        try {
            TeamMember clone = (TeamMember) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
