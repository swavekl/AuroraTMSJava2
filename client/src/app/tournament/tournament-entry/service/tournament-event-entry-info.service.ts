import {Injectable} from '@angular/core';
import {EntityActionOptions, EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {Observable} from 'rxjs';
import {TournamentEventEntryInfoDataService} from './tournament-event-entry-info-data.service';

@Injectable({
  providedIn: 'root'
})
export class TournamentEventEntryInfoService extends EntityCollectionServiceBase<TournamentEventEntryInfo> {

  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory,
              private tournamentEventEntryInfoDataService: TournamentEventEntryInfoDataService) {
    super('TournamentEventEntryInfo', serviceElementsFactory);
  }

  loadForTournamentEntry(tournamentEntryId: number, options?: EntityActionOptions): Observable<TournamentEventEntryInfo[]> {
    this.tournamentEventEntryInfoDataService.setTournamentEntryId(tournamentEntryId);
    return super.load(options);
  }
}
