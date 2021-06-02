import {EventEntryStatus} from './event-entry-status.enum';
import {AvailabilityStatus} from './availability-status.enum';
import {EventEntryCommand} from './event-entry-command.enum';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';

/**
 * Combines information about the event,  event entry and its status.  Used to display information during tournament
 * registration.
 * Objects of this class are not cached in NGRX store.
 */
export class TournamentEventEntryInfo {
  // id of event this entry info is for
  eventFk: number;

  // id of event entry if entered, null otherwise
  eventEntryFk: number;

  // current entry status
  status: EventEntryStatus = EventEntryStatus.NOT_ENTERED;

  // status explaining why event is or is not available for entry
  availabilityStatus: AvailabilityStatus = AvailabilityStatus.AVAILABLE_FOR_ENTRY;

  // command user can execute to change event state from current state to next state
  eventEntryCommand: EventEntryCommand = EventEntryCommand.NO_COMMAND;

  // price to pay for event - may be different by age or by some other pricing algorithm
  price: number;

  // full event definition matching the eventFK.  Used to access information such as day & time of event etc.
  event: TournamentEvent;

  // if this entry is into doubles event - this is doubles partner profile id and represents requested partner
  // pairing up is done only after both players agree to play as a team see DoublesPair class
  doublesPartnerProfileId: string;

  // full name of doubles partner if selected
  doublesPartnerName: string;

}
