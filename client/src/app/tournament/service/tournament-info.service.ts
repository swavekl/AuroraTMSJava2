import {Injectable} from '@angular/core';

import {EntityActionOptions, EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {TournamentInfo} from '../model/tournament-info.model';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class TournamentInfoService extends EntityCollectionServiceBase<TournamentInfo> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('TournamentInfo', serviceElementsFactory);
  }

  getAll(options?: EntityActionOptions): Observable<TournamentInfo[]> {
    return super.getAll(options)
      .pipe(map(tournamentInfos => tournamentInfos.map(tournamentInfo => TournamentInfo.convert(tournamentInfo))));
  }

  getAllRecentAndFuture(date: Date): Observable<TournamentInfo[]> {
    const params = `date=${date}`;
    return super.getWithQuery(params)
      .pipe(map(tournamentInfos => tournamentInfos.map(tournamentInfo => TournamentInfo.convert(tournamentInfo))));
  }

  getByKey(key: any, options?: EntityActionOptions): Observable<TournamentInfo> {
    return super.getByKey(key, options)
      .pipe(map(tournamentInfo => TournamentInfo.convert(tournamentInfo)));
  }
}

