import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {distinctUntilChanged, map, tap} from 'rxjs/operators';
import {PlayerScheduleItem} from '../model/player-schedule-item.model';

@Injectable({
  providedIn: 'root'
})
export class PlayerScheduleService {

  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean>;

  constructor(private httpClient: HttpClient) {
    this.loading$ = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  /**
   * Checks the status of user connected account
   * @param tournamentEntryId entry id for wich to get the schedule
   * @param userProfileId user profile id
   */
  public getFullPlayerSchedule(tournamentEntryId: number, userProfileId: string): Observable<PlayerScheduleItem[]> {
    const url = `/api/playerschedule/${tournamentEntryId}/${userProfileId}`;
    this.setLoading(true);
    return this.httpClient.get<PlayerScheduleItem[]>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }),
        map((response: PlayerScheduleItem[]) => {
            return response;
          }
        )
      );
  }

  /**
   * Details of one match or RR group
   * @param matchCardId
   */
  public getPlayerScheduleDetail(matchCardId: number): Observable<PlayerScheduleItem> {
    const url = `/api/playerschedule/detail/${matchCardId}`;
    this.setLoading(true);
    return this.httpClient.get<PlayerScheduleItem>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }),
        map((response: PlayerScheduleItem) => {
            return response;
          }
        )
      );
  }
}
