package com.auroratms.tournamentevententry;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TournamentEventEntryInfo {

    // id of event this entry info is for
    private Long eventFk;

    // id of event entry if entered, null otherwise
    private Long eventEntryFk;

    // current entry status
    private EventEntryStatus status = EventEntryStatus.NOT_ENTERED;

    // status explaining why event is or is not available for entry
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE_FOR_ENTRY;

    // command user can execute to change event state from current state
    private EventEntryCommand eventEntryCommand = EventEntryCommand.NO_COMMAND;

    // price to pay for event - may be different by age or by some other pricing algorithm
    private double price;

    // if this entry is into doubles event - this is doubles partner profile id and represents requested partner
    // pairing up is done only after both players agree to play as a team see DoublesPair class
    private String doublesPartnerProfileId;

    private String doublesPartnerName;

    // id of the shopping cart session for this change so we can update last time cart was changed
    private String cartSessionId;
}
