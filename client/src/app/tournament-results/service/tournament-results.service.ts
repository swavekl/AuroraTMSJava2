import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {distinctUntilChanged, map, tap} from 'rxjs/operators';
import {AccountStatus} from '../../account/service/account.service';
import {EventResultStatus} from '../model/event-result-status';
import {EventResults} from '../model/event-results';
import {PlayerMatchSummary} from '../model/player-match-summary';

@Injectable({
  providedIn: 'root'
})
export class TournamentResultsService {

  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean>;

  constructor(private httpClient: HttpClient) {
    this.loading$ = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  /**
   * Gets a list of events and their results status for a specified tournament
   * @param tournamentId
   */
  public getTournamentResults(tournamentId: number): Observable<EventResultStatus[]> {
    const url = `/api/tournamentresults/${tournamentId}`;
    this.setLoading(true);
    return this.httpClient.get<EventResultStatus[]>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }),
        map((response: EventResultStatus[]) => {
            // console.log('response ' + JSON.stringify(response));
            return response;
          }
        )
      );
  }

  /**
   * Gets a list of events and their results status for a specified tournament
   * @param tournamentId
   * @param eventId
   */
  public getEventResults(tournamentId: number, eventId: number): Observable<EventResults[]> {
    const url = `/api/tournamentresults/${tournamentId}/event/${eventId}`;
    this.setLoading(true);
    return this.httpClient.get<EventResults[]>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }),
        map((response: EventResults[]) => {
            // console.log('EventResults response ' + JSON.stringify(response));
            return response;
          }
        )
      );
  }

  /**
   * Gets a list of completed matches and their results for a player in a tournament
   * @param tournamentId
   * @param entryId
   * @param profileId
   */
  public getPlayerTournamentResults(entryId: number, profileId: string): Observable<PlayerMatchSummary[]> {
    const url = `/api/tournamentresults/entry/${entryId}/${profileId}`;
    this.setLoading(true);
    return this.httpClient.get<PlayerMatchSummary[]>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }),
        map((response: PlayerMatchSummary[]) => {
            // console.log('PlayerMatchSummary response ' + JSON.stringify(response));
            return response;
          }
        )
      );
  }
}
