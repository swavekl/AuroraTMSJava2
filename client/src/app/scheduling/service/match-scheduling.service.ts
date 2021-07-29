import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {distinctUntilChanged, map, tap} from 'rxjs/operators';
import {UsattPlayerRecord} from '../../profile/model/usatt-player-record.model';
import {MatchCard} from '../../matches/model/match-card.model';

@Injectable({
  providedIn: 'root'
})
export class MatchSchedulingService {

  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean>;

  constructor(private http: HttpClient) {
    this.loading$ = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());
  }

  private setLoading(loading: boolean) {
    // console.log ('setLoading ', loading);
    this.indicatorSubject$.next(loading);
  }

  public generateScheduleForTournamentAndDay(tournamentId: number, day: number): Observable<MatchCard[]> {
    const url = `/api/schedule/${tournamentId}/${day}`;
    this.setLoading(true);
    return this.http.get<MatchCard[]>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }),
        map((matchCards: MatchCard[]) => {
            if (matchCards) {
              return matchCards;
            }
          }
        )
      );
  }
}


