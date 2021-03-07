package com.auroratms.tournamentevententry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "tournamentevententry", uniqueConstraints = {
        @UniqueConstraint(name = "idx_unique_event_entry", columnNames = {"tournamentFk", "tournamentEventFk", "status"})
})
@NoArgsConstructor
@Getter
@Setter
public class TournamentEventEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long tournamentFk;

    @Column(nullable = false)
    private long tournamentEntryFk;

    @Column(nullable = false)
    private long tournamentEventFk;

    // date user entered the event - for waiting list order
    private Date dateEntered;

    // status of event entry
    private EventEntryStatus status;

    // session id for deleting
    private String entrySessionId;

    // price player needs to pay for the event - seniors vs juniors may be different
    private double price;
}
