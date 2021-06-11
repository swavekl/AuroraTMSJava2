import {Injectable} from '@angular/core';
import {EntityActionOptions, EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {TournamentEventEntry} from '../model/tournament-event-entry.model';
import {Observable} from 'rxjs';
import {TournamentEventEntryDataService} from '../../tournament-config/tournament-event-entry-data.service';
import {map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class TournamentEventEntryService extends EntityCollectionServiceBase<TournamentEventEntry> {

  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory,
              private tournamentEventEntryDataService: TournamentEventEntryDataService) {
    super('TournamentEventEntry', serviceElementsFactory);
  }

  /**
   * Loads all entries for one player (i.e. via his tournament entry id)
   * @param tournamentEntryId
   * @param options
   */
  loadAllForTournamentEntry(tournamentEntryId: number, options?: EntityActionOptions): Observable<TournamentEventEntry[]> {
    this.tournamentEventEntryDataService.setTournamentEntryId(tournamentEntryId);
    return super.load(options);
  }

  /**
   * Loads all entries for a given event
   * @param eventId
   * @param options
   */
  loadEntriesForEvent(eventId: number, options?: EntityActionOptions): Observable<TournamentEventEntry[]> {
    super.clearCache();
    return this.tournamentEventEntryDataService.listForEvent(eventId)
      .pipe(
        map((entries: TournamentEventEntry[], index: number): TournamentEventEntry[] => {
          super.addAllToCache(entries);
          return entries;
        })
      );
  }
}
