import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {distinctUntilChanged, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {TournamentEntryInfo} from '../model/tournament-entry-info.model';

@Injectable({
  providedIn: 'root'
})
export class TournamentEntryInfoService {

  private loadingSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean> = this.loadingSubject$.asObservable().pipe(distinctUntilChanged());

  constructor(private httpClient: HttpClient) {
  }

  private setLoading(loading: boolean) {
    this.loadingSubject$.next(loading);
  }

  getAll(tournamentId: number): Observable<TournamentEntryInfo []> {
    this.setLoading(true);
    const url = `/api/tournamentplayers/${tournamentId}`;
    return this.httpClient.get<TournamentEntryInfo []>(url)
      .pipe(
        tap(
          () => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }
        )
      );
  }

  /**
   * Gets all tournament entry infos for one event
   * @param eventId
   */
  getAllForEvent(eventId: number): Observable<TournamentEntryInfo[]> {
    this.setLoading(true);
    const url = `/api/eventplayers/${eventId}`;
    return this.httpClient.get<TournamentEntryInfo []>(url)
      .pipe(
        tap(
          () => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }
        )
      );
  }
}
