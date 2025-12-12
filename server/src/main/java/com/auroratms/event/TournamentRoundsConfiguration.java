package com.auroratms.event;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Additional information which will not be queryable.  This will allow us to add new configuration data in the future easily
 */
@NoArgsConstructor
@Getter
@Setter
public class TournamentRoundsConfiguration implements Serializable {
    // to have more than one round and multiple divisions within round
    private List<TournamentEventRound> rounds;

    public TournamentRoundsConfiguration(TournamentRoundsConfiguration fromConfiguration) {
        if (fromConfiguration.rounds != null) {
            this.rounds = new ArrayList<>(fromConfiguration.rounds.size());
            for (TournamentEventRound round : fromConfiguration.rounds) {
                TournamentEventRound tournamentRoundCopy = new TournamentEventRound(round);
                this.rounds.add(tournamentRoundCopy);
            }
        }
    }
}
