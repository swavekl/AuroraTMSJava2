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

  loadTournamentDoublesEvents(tournamentId: number, options?: EntityActionOptions): Observable<TournamentEvent[]> {
    this.tournamentEventConfigDataService.setTournamentId(tournamentId);
    const queryParams = 'doublesOnly=true';
    return super.getWithQuery(queryParams)
      .pipe(map(tournamentEvents => tournamentEvents
        .map(tournamentEvent => TournamentEvent.convert(tournamentEvent))));
  }


  loadTournamentEvents(tournamentId: number, options?: EntityActionOptions): Observable<TournamentEvent[]> {
    this.tournamentEventConfigDataService.setTournamentId(tournamentId);
    return super.load(options);
  }

  saveTournamentEvent(tournamentId: number, tournamentEvent: TournamentEvent,
                      options?: EntityActionOptions): Observable<TournamentEvent> {
    this.tournamentEventConfigDataService.setTournamentId(tournamentId);
    return super.upsert(tournamentEvent, options)
      .pipe(map(event => TournamentEvent.convert(event)));
  }

  getByKey(tournamentId: number, key: any, options?: EntityActionOptions): Observable<TournamentEvent> {
    this.tournamentEventConfigDataService.setTournamentId(tournamentId);
    return super.getByKey(key, options)
      .pipe(map(tournamentEvent => TournamentEvent.convert(tournamentEvent)));
  }
}
