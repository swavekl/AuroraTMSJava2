import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {distinctUntilChanged, finalize, map, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';
import {DateUtils} from '../../shared/date-utils';

@Injectable({
  providedIn: 'root'
})
export class UsattPlayerRecordService {

  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean>;

  constructor(private http: HttpClient) {
    this.loading$ = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());
  }

  private setLoading(loading: boolean) {
    // console.log ('setLoading ', loading);
    this.indicatorSubject$.next(loading);
  }

  public searchByNames(firstName: string, lastName: string, pageNum: number): Observable<UsattPlayerRecord []> {
    const searchCriteria = [
      {name: 'firstName', value: firstName},
      {name: 'lastName', value: lastName}
    ];

    let filter = '';
    for (const searchCriterion of searchCriteria) {
      filter += (filter.length === 0) ? '?' : '&';
      filter += searchCriterion.name + '=' + searchCriterion.value;
    }
    filter += (filter.length === 0) ? '?' : '&';
    filter += `page=${pageNum}`;

    const url = `/api/usattplayers${filter}`;
    this.setLoading(true);
    return this.http.get<UsattPlayerRecord[]>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }),
        map((records: UsattPlayerRecord[]) => {
            for (let i = 0; i < records.length; i++) {
              const record = records[i];
              this.fixResult(record);
            }
            return records;
          }
        )
      );
  }

  public getByNames(firstName: string, lastName: string): Observable<UsattPlayerRecord> {
    const searchCriteria = [
      {name: 'firstName', value: firstName},
      {name: 'lastName', value: lastName}
    ];
    return this.getOneByCriteria(searchCriteria);
  }

  public getByMembershipId(membershipId: number): Observable<UsattPlayerRecord> {
    const searchCriteria = [
      {name: 'membershipId', value: membershipId}
    ];
    return this.getOneByCriteria(searchCriteria);
  }

  /**
   * Find usatt records given filter expression e.g. firstName=John&lastName=Glen
   * @param searchCriteria expression
   */
  getOneByCriteria(searchCriteria: any[]): Observable<UsattPlayerRecord> {
    let filter = '';
    for (const searchCriterion of searchCriteria) {
      filter += (filter.length === 0) ? '?' : '&';
      filter += searchCriterion.name + '=' + searchCriterion.value;
    }
    const url = `/api/usattplayer${filter}`;
    this.setLoading(true);
    return this.http.get<UsattPlayerRecord>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }),
        map((record: UsattPlayerRecord) => {
            if (record) {
              this.fixResult(record);
              // convert date strings into Date objects
              return record;
            }
          }
        )
      );
  }

  private fixResult(record: UsattPlayerRecord): void {
    const dateUtils = new DateUtils();
    record.membershipExpiration = dateUtils.convertFromString(record.membershipExpiration);
    record.dateOfBirth = dateUtils.convertFromString(record.dateOfBirth);
    record.lastLeaguePlayedDate = dateUtils.convertFromString(record.lastLeaguePlayedDate);
    record.lastTournamentPlayedDate = dateUtils.convertFromString(record.lastTournamentPlayedDate);
  }
}
