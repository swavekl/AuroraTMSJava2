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
}
