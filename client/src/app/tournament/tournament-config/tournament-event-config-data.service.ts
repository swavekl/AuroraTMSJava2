import {Injectable} from '@angular/core';
import {DefaultDataService, DefaultDataServiceConfig, HttpUrlGenerator} from '@ngrx/data';
import {TournamentEvent} from './tournament-event.model';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {of} from 'rxjs';
import {QueryParams} from '@ngrx/data/src/dataservices/interfaces';

@Injectable({providedIn: 'root'})
export class TournamentEventConfigDataService extends DefaultDataService<TournamentEvent> {

  // id of the currently queried tournament
  private tournamentId: number;

  constructor(http: HttpClient, httpUrlGenerator: HttpUrlGenerator, config?: DefaultDataServiceConfig) {
    super('TournamentEvent', http, httpUrlGenerator, config);
  }

  setTournamentId(tournamentId: number) {
    this.tournamentId = tournamentId;
  }

  getAll(): Observable<TournamentEvent[]> {
    if (this.tournamentId != null) {
      const url = '/api/tournament/' + this.tournamentId + '/tournamentevents';
      console.log('url ', url);
      return this.execute('GET', url);
    } else {
      const emptyArray: TournamentEvent[] = new TournamentEvent [0];
      return of(emptyArray);
    }
  }

  getWithQuery(queryParams: QueryParams | string): Observable<TournamentEvent[]> {
    if (this.tournamentId != null) {
      const url = '/api/tournament/' + this.tournamentId + '/tournamentevents?' + queryParams;
      console.log('get url ', url);
      return this.execute('GET', url);
    }
  }

  getById(key: number | string): Observable<TournamentEvent> {
    if (this.tournamentId != null) {
      const url = '/api/tournament/' + this.tournamentId + '/tournamentevent/' + key;
      console.log('getById url ', url);
      return this.execute('GET', url);
    }
  }

  upsert(tournamentEvent: TournamentEvent): Observable<TournamentEvent> {
    if (this.tournamentId != null) {
      if (tournamentEvent.id == null) {
        const url = '/api/tournament/' + this.tournamentId + '/tournamentevent';
        console.log('upsert POST url ', url);
        return this.execute('POST', url, tournamentEvent);
      } else {
        const url = '/api/tournament/' + this.tournamentId + '/tournamentevent/' + tournamentEvent.id;
        console.log('upsert PUT url ', url);
        return this.execute('PUT', url, tournamentEvent);
      }
    }
  }

  delete(key: number | string): Observable<number | string> {
    if (this.tournamentId != null) {
      const url = '/api/tournament/' + this.tournamentId + '/tournamentevent/' + key;
      console.log('delete url ', url);
      return this.execute('DELETE', url);
    }
  }
}
