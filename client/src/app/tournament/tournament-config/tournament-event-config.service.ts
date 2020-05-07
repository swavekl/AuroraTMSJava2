import {Injectable} from '@angular/core';

import {EntityActionOptions, EntityCollectionServiceBase, EntityCollectionServiceElementsFactory, QueryParams} from '@ngrx/data';
import {TournamentEvent} from './tournament-event.model';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TournamentEventConfigDataService} from './tournament-event-config-data.service';

@Injectable({providedIn: 'root'})
export class TournamentEventConfigService extends EntityCollectionServiceBase<TournamentEvent> {

  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory,
              private tournamentEventConfigDataService: TournamentEventConfigDataService) {
    super('TournamentEvent', serviceElementsFactory);
  }

  getAllForTournament(tournamentId: number, options?: EntityActionOptions): Observable<TournamentEvent[]> {
    this.tournamentEventConfigDataService.setTournamentId(tournamentId);
    // return super.getAll(options)
    const queryParams = 'size=10&page=0&sort=ordinalNumber,asc';
    return super.getWithQuery(queryParams)
      .pipe(map(tournamentEvents => tournamentEvents
        .map(tournamentEvent => TournamentEvent.convert(tournamentEvent))));
  }

  getByKey(tournamentId: number, key: any, options?: EntityActionOptions): Observable<TournamentEvent> {
    this.tournamentEventConfigDataService.setTournamentId(tournamentId);
    return super.getByKey(key, options)
      .pipe(map(tournamentEvent => TournamentEvent.convert(tournamentEvent)));
  }
}
