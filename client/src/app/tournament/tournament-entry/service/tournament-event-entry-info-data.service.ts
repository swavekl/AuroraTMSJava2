import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {DefaultDataService, DefaultDataServiceConfig, HttpUrlGenerator} from '@ngrx/data';
import {TournamentEventEntry} from '../model/tournament-event-entry.model';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {Observable, of} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TournamentEventEntryInfoDataService extends DefaultDataService<TournamentEventEntryInfo> {

  // id of the currently queried tournament entry
  private tournamentEntryId: number;

  constructor(http: HttpClient,
              httpUrlGenerator: HttpUrlGenerator,
              config?: DefaultDataServiceConfig) {
    super('TournamentEventEntryInfo', http, httpUrlGenerator, config);
  }

  setTournamentEntryId(tournamentEntryId: number) {
    this.tournamentEntryId = tournamentEntryId;
  }

  private getServiceUrl(plural: boolean = false) {
    const strPlural = (plural) ? 'tournamentevententryinfos' : 'tournamentevententryinfo';
    return `/api/tournamententry/${this.tournamentEntryId}/${strPlural}`;
  }

  getAll(): Observable<TournamentEventEntryInfo[]> {
    if (this.tournamentEntryId != null) {
      const url = this.getServiceUrl(true);
      return this.execute('GET', url);
    } else {
      const emptyArray: TournamentEventEntryInfo[] = new TournamentEventEntryInfo [0];
      return of(emptyArray);
    }
  }
}
