package com.auroratms.team;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "team", indexes = {
        @Index(name = "idx_tournament_event", columnList = "tournamentEventFk")
})
@NoArgsConstructor
@Getter
@Setter
public class Team implements Serializable {
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
    private int rating;

    // list of team members of this team
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Tells Jackson to serialize the list of members
    private List<TeamMember> teamMembers = new java.util.ArrayList<>();
}
