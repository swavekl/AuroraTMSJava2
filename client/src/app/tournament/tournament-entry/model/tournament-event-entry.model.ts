import {EventEntryStatus} from './event-entry-status.enum';

export class TournamentEventEntry {
  id: number;

  tournamentFk: number;

  tournamentEntryFk: number;

  tournamentEventFk: number;

  dateEntered: Date;

  // status of event entry
  status: EventEntryStatus = EventEntryStatus.NOT_ENTERED;

  // session id for deleting
  entrySessionId: string;

  // price player needs to pay for the event - seniors vs juniors may be different
  price: number;

  // if this entry is into doubles event - this is doubles partner profile id and represents requested partner
  // pairing up is done only after both players agree to play as a team see DoublesPair class
  doublesPartnerProfileId: string;

}
