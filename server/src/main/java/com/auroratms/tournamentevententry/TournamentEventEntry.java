package com.auroratms.tournamentevententry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "tournamentevententry", uniqueConstraints = {
        @UniqueConstraint(name = "idx_unique_event_entry", columnNames = {"tournamentFk", "tournamentEventFk", "tournamentEntryFk", "status"})
})
@NoArgsConstructor
@Getter
@Setter
public class TournamentEventEntry implements Serializable {
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
    @Column(length = 36)
    private String cartSessionId;

    // price player needs to pay for the event - seniors vs juniors may be different
    private double price;

    // if this entry is into doubles event - this is doubles partner profile id and represents requested partner
    // pairing up is done only after both players agree to play as a team see DoublesPair class
    private String doublesPartnerProfileId;

    @Override
    public String toString() {
        return "TournamentEventEntry{" +
                "id=" + id +
                ", tournamentFk=" + tournamentFk +
                ", tournamentEntryFk=" + tournamentEntryFk +
                ", tournamentEventFk=" + tournamentEventFk +
                ", dateEntered=" + dateEntered +
                ", status=" + status +
                ", cartSessionId='" + cartSessionId + '\'' +
                ", price=" + price +
                ", doublesPartnerProfileId='" + doublesPartnerProfileId + '\'' +
                '}';
    }
}
