import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {distinctUntilChanged, map, tap} from 'rxjs/operators';
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

  public clearScheduleForTournamentAndDay(tournamentId: number, day: number): Observable<MatchCard[]> {
    const url = `/api/schedule/clear/${tournamentId}/${day}`;
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

  public generateScheduleForMatchCards(tournamentId: number, day: number, matchCardIds: number []): Observable<MatchCard[]> {
    const url = `/api/schedule/${tournamentId}/${day}/regenerate`;
    this.setLoading(true);
    return this.http.put<MatchCard[]>(url, matchCardIds)
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

  public getScheduleForTournamentDayAndTable(tournamentId: number, day: number, tableNumber: number): Observable<MatchCard[]> {
    const url = `/api/schedule/${tournamentId}/${day}/table/${tableNumber}`;
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

  public updateMatchCards(matchCards: MatchCard[]): Observable<void> {
    const url = `/api/schedule`;
    this.setLoading(true);
    return this.http.put<void>(url, matchCards, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    })
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }
        )
      );
  }
}


