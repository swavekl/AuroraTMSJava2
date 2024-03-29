import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, of, Subject} from 'rxjs';
import {distinctUntilChanged, first, map, switchMap, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {OriginalEntryInfo} from '../model/original-entry-info';

/**
 * Plain service whose results are not going to be cached by ngrx
 */
@Injectable({
  providedIn: 'root'
})
export class EventEntryInfoService {

  // loading indicator just like in other services - used during load and save
  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean> = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());

  // setup durable observable so we can notify via same channel to the caller
  private entitiesSubject$: Subject<TournamentEventEntryInfo[]> = new Subject<TournamentEventEntryInfo[]>();
  eventEntryInfos$: Observable<TournamentEventEntryInfo[]> = this.entitiesSubject$.asObservable();

  constructor(private http: HttpClient) {
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  /**
   *
   * @param tournamentEntryId
   */
  public getEventEntryInfos(tournamentEntryId: number): void {
    this.setLoading(true);
    const url = `/api/tournamententry/${tournamentEntryId}/eventstatus/list`;
    this.http.get<TournamentEventEntryInfo[]>(url)
      .pipe(
        first(),
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }),
        switchMap((result: TournamentEventEntryInfo[]): Observable<boolean> => {
          this.entitiesSubject$.next(result);
          return of(true);
        })
      )
      .subscribe(
        (value: boolean): void => {
    });
  }

  /**
   *
   * @param tournamentEntryId
   * @param tournamentEventEntryInfo
   */
  public changeEntryStatus(tournamentEntryId: number, tournamentEventEntryInfo: TournamentEventEntryInfo): Observable<boolean> {
    this.setLoading(true);
    const url = `/api/tournamententry/${tournamentEntryId}/eventstatus/change`;
    return this.http.post(url, tournamentEventEntryInfo)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }),
        map((response: Response) => {
          return true;
            // return (response.status === 200);
          })
      );
  }

  /**
   * Confirms all entries for this entry
   *
   * @param tournamentEntryId
   * @param cartSessionId
   * @param withdrawing
   */
  public confirmEntries(tournamentEntryId: number, cartSessionId: string, withdrawing: boolean): Observable<boolean> {
    this.setLoading(true);
    const url = `/api/tournamententry/${tournamentEntryId}/eventstatus/confirmall/${cartSessionId}?withdrawing=${withdrawing}`;
    return this.http.put(url, {})
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }),
        map((response: Response) => {
          return true;
          // return (response.status === 200);
        })
      );
  }

  /**
   * Confirms all entries for this entry
   *
   * @param tournamentEntryId
   * @param cartSessionId
   * @param withdrawing
   */
  public discardChanges(discardChangesInfo: OriginalEntryInfo): Observable<boolean> {
    this.setLoading(true);
    const url = `/api/tournamententry/${discardChangesInfo.entryId}/eventstatus/discard`;
    return this.http.put(url, discardChangesInfo)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }),
        map((response: Response) => {
          return true;
          // return (response.status === 200);
        })
      );
  }
}
