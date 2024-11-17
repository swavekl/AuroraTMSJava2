import {Injectable} from '@angular/core';
import {DefaultDataService, DefaultDataServiceConfig, HttpUrlGenerator} from '@ngrx/data';
import {TournamentEvent} from './tournament-event.model';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {of} from 'rxjs';
import {QueryParams} from '@ngrx/data/src/dataservices/interfaces';
import {AuthenticationService} from '../../user/authentication.service';

@Injectable({providedIn: 'root'})
export class TournamentEventConfigDataService extends DefaultDataService<TournamentEvent> {

  // id of the currently queried tournament
  private tournamentId: number;

  constructor(http: HttpClient,
              httpUrlGenerator: HttpUrlGenerator,
              private authenticationService: AuthenticationService,
              config?: DefaultDataServiceConfig) {
    super('TournamentEvent', http, httpUrlGenerator, config);
  }

  setTournamentId(tournamentId: number) {
    this.tournamentId = tournamentId;
  }

  getServiceUrl(plural: boolean = false): string {
    const currentUser = this.authenticationService.getCurrentUser();
    const urlStart = (currentUser != null) ? '/api' : '/publicapi'
    const addPlural = (plural) ? 's' : '';
    return `${urlStart}/tournament/${this.tournamentId}/tournamentevent${addPlural}`;
  }

  getAll(): Observable<TournamentEvent[]> {
    if (this.tournamentId != null) {
      const url = this.getServiceUrl(true) + '?page=0&size=200';
      return this.execute('GET', url);
    } else {
      const emptyArray: TournamentEvent[] = new TournamentEvent [0];
      return of(emptyArray);
    }
  }

  getWithQuery(queryParams: QueryParams | string): Observable<TournamentEvent[]> {
    if (this.tournamentId != null) {
      const url = this.getServiceUrl(true) + '?' + queryParams;
      return this.execute('GET', url);
    }
  }

  getById(key: number | string): Observable<TournamentEvent> {
    if (this.tournamentId != null) {
      const url = this.getServiceUrl() + '/' + key;
      return this.execute('GET', url);
    }
  }

  upsert(tournamentEvent: TournamentEvent): Observable<TournamentEvent> {
    if (this.tournamentId != null) {
      if (tournamentEvent.id == null) {
        const url = this.getServiceUrl();
        return this.execute('POST', url, tournamentEvent);
      } else {
        const url = this.getServiceUrl() + '/' + tournamentEvent.id;
        return this.execute('PUT', url, tournamentEvent);
      }
    }
  }

  delete(key: number | string): Observable<number | string> {
    if (this.tournamentId != null) {
      const url = this.getServiceUrl() + '/' + key;
      return this.execute('DELETE', url);
    }
  }
}
