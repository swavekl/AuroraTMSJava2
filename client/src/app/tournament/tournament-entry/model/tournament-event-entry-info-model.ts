import {TournamentEventEntry} from './tournament-event-entry.model';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';

/**
 * combines the information about the event and entry
 */
export class TournamentEventEntryInfo {
  id: number;
  eventEntry: TournamentEventEntry;
  event: TournamentEvent;
}
