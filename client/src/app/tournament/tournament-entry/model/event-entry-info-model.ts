import {TournamentEventEntry} from './tournament-event-entry.model';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';

/**
 * combines the information about the event and entry
 */
export class EventEntryInfo {
  eventEntry: TournamentEventEntry;
  event: TournamentEvent;
}
