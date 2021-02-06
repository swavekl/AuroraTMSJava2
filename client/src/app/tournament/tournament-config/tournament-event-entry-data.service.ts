import {Injectable} from '@angular/core';
import {DefaultDataService, DefaultDataServiceConfig, HttpUrlGenerator} from '@ngrx/data';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {of} from 'rxjs';
import {QueryParams} from '@ngrx/data/src/dataservices/interfaces';
import {TournamentEventEntry} from '../tournament-entry/model/tournament-event-entry.model';

@Injectable({providedIn: 'root'})
export class TournamentEventEntryDataService extends DefaultDataService<TournamentEventEntry> {

  // id of the currently queried tournament entry
  private tournamentEntryId: number;

  constructor(http: HttpClient,
              httpUrlGenerator: HttpUrlGenerator,
              config?: DefaultDataServiceConfig) {
    super('TournamentEventEntry', http, httpUrlGenerator, config);
  }

  setTournamentEntryId(tournamentEntryId: number) {
    this.tournamentEntryId = tournamentEntryId;
  }

  private getServiceUrl() {
    return `/api/tournamententry/${this.tournamentEntryId}/tournamentevententry`;
  }

  getAll(): Observable<TournamentEventEntry[]> {
    if (this.tournamentEntryId != null) {
      const url = this.getServiceUrl();
      // console.log('getAll url ', url);
      return this.execute('GET', url);
    } else {
      const emptyArray: TournamentEventEntry[] = new TournamentEventEntry [0];
      return of(emptyArray);
    }
  }

  getWithQuery(queryParams: QueryParams | string): Observable<TournamentEventEntry[]> {
    if (this.tournamentEntryId != null) {
      const url = this.getServiceUrl() + '?' + queryParams;
      // console.log('get url ', url);
      return this.execute('GET', url);
    }
  }

  getById(key: number | string): Observable<TournamentEventEntry> {
    if (this.tournamentEntryId != null) {
      const url = this.getServiceUrl() + '/' + key;
      // console.log('getById url ', url);
      return this.execute('GET', url);
    }
  }

  upsert(tournamentEventEntry: TournamentEventEntry): Observable<TournamentEventEntry> {
    if (this.tournamentEntryId != null) {
      if (tournamentEventEntry.id == null) {
        const url = this.getServiceUrl();
        // console.log('upsert POST url ', url);
        return this.execute('POST', url, tournamentEventEntry);
      } else {
        const url = this.getServiceUrl() + '/' + tournamentEventEntry.id;
        // console.log('upsert PUT url ', url);
        return this.execute('PUT', url, tournamentEventEntry);
      }
    }
  }

  delete(key: number | string): Observable<number | string> {
    if (this.tournamentEntryId != null) {
      const url = this.getServiceUrl() + '/' + key;
      // console.log('delete url ', url);
      return this.execute('DELETE', url);
    }
  }
}
