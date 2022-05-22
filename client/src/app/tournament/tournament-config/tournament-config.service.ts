import {Injectable} from '@angular/core';

import {EntityActionOptions, EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {Tournament} from './tournament.model';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class TournamentConfigService extends EntityCollectionServiceBase<Tournament> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('Tournament', serviceElementsFactory);
  }

  getAll(options?: EntityActionOptions): Observable<Tournament[]> {
    return super.getAll(options)
      .pipe(map(tournaments => tournaments.map(tournament => Tournament.convert(tournament))));
  }

  getTodaysTournaments(today: Date): Observable<Tournament[]> {
    return super.getWithQuery(`today=${today}`);
  }

  getByKey(key: any, options?: EntityActionOptions): Observable<Tournament> {
    return super.getByKey(key, options)
      .pipe(map(tournament => Tournament.convert(tournament)));
  }

  clone(tournament: Tournament): Observable<Tournament> {
    return super.add(tournament)
      .pipe(map(savedTournament => Tournament.convert(savedTournament)));
  }
}
