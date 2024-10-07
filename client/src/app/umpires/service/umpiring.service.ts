import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {distinctUntilChanged, map, tap} from 'rxjs/operators';

import {UmpireWork} from '../model/umpire-work.model';
import {UmpireWorkSummary} from '../model/umpire-work-summary.model';
import {UmpiredMatchInfo} from '../model/umpired-match-info.model';

@Injectable({
  providedIn: 'root'
})
export class UmpiringService {
  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean>;

  constructor(private httpClient: HttpClient) {
    this.loading$ = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  public assign(umpireWork: UmpireWork): Observable<void> {
    const url = `/api/umpire/assign`;
    this.setLoading(true);
    return this.httpClient.post<UmpireWork>(url, umpireWork)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }),
        map((response: any) => {
            return response;
          }
        )
      );
  }

  public getSummaries(tournamentId: number): Observable<UmpireWorkSummary[]> {
    const url = `/api/umpire/summary/${tournamentId}`;
    return this.httpClient.get(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }),
        map((response: UmpireWorkSummary[]) => {
            return response;
          }
        )
      );
  }

  public getUmpireMatches(umpireProfileId: string): Observable<UmpiredMatchInfo[]> {
    const url = `/api/umpire/matches/${umpireProfileId}`;
    return this.httpClient.get(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }),
        map((response: UmpiredMatchInfo[]) => {
            return response;
          }
        )
      );
  }

}
