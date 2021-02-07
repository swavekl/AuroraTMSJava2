import {Injectable} from '@angular/core';
import {EntityActionOptions, EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {TournamentEventEntry} from '../model/tournament-event-entry.model';
import {Observable} from 'rxjs';
import {TournamentEventEntryDataService} from '../../tournament-config/tournament-event-entry-data.service';

@Injectable({
  providedIn: 'root'
})
export class TournamentEventEntryService extends EntityCollectionServiceBase<TournamentEventEntry> {

  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory,
              private tournamentEventEntryDataService: TournamentEventEntryDataService) {
    super('TournamentEventEntry', serviceElementsFactory);
  }

  getAllForTournamentEntry(tournamentEntryId: number, options?: EntityActionOptions): Observable<TournamentEventEntry[]> {
    this.tournamentEventEntryDataService.setTournamentEntryId(tournamentEntryId);
    return super.load(options);
  }
}
