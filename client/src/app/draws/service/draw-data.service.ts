import { Injectable } from '@angular/core';
import {DefaultDataService, DefaultDataServiceConfig, HttpUrlGenerator} from '@ngrx/data';
import {TournamentEventEntry} from '../../tournament/tournament-entry/model/tournament-event-entry.model';
import {HttpClient} from '@angular/common/http';
import {Draw} from '../model/draw.model';
import {Observable} from 'rxjs';
import {DrawType} from '../model/draw-type.enum';

@Injectable({
  providedIn: 'root'
})
export class DrawDataService extends DefaultDataService<Draw>{

  constructor(http: HttpClient,
              httpUrlGenerator: HttpUrlGenerator,
              config?: DefaultDataServiceConfig) {
    super('Draw', http, httpUrlGenerator, config);
  }

  /**
   * Generate all draws
   * @param eventId
   * @param drawType
   */
  generate(eventId: number, drawType: DrawType): Observable<Draw[]> {
    const queryParams = `eventId=${eventId}&drawType=${drawType.valueOf()}`;
    const url = `/api/draws?${queryParams}`;
    return this.execute('POST', url);
  }

  /**
   *
   * @param eventId
   */
  deleteForEvent(eventId: number): Observable<number> {
    const url = `/api/draws?eventId=${eventId}`;
    return this.execute('DELETE', url);
  }

}
