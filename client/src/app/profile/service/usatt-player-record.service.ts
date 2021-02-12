import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {distinctUntilChanged, map} from 'rxjs/operators';
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

  public searchByNames (firstName: string, lastName: string): Observable<UsattPlayerRecord []> {
    const searchCriteria = [
      {name: 'firstName', value: firstName},
      {name: 'lastName', value: lastName}
    ];
    // search just once maybe we get lucky
    return this.search(searchCriteria);
  }

  public searchByMembershipId (membershipId: number): Observable<UsattPlayerRecord []> {
    const searchCriteria = [
      {name: 'membershipId', value: membershipId}
    ];
    // search just once maybe we get lucky
    return this.search(searchCriteria);
  }

  /**
   * Find usatt records given filter expression e.g. firstName=John&lastName=Glen
   * @param searchCriteria expression
   */
  search(searchCriteria: any[]): Observable<UsattPlayerRecord []> {
    let filter = '';
    for (const searchCriterion of searchCriteria) {
      filter += (filter.length === 0) ? '?' : '&';
      filter += searchCriterion.name + '=' + searchCriterion.value;
    }
    const url = `/api/usattplayers${filter}`;
    const dateUtils = new DateUtils ();
    return this.http.get<UsattPlayerRecord[]>(url)
      .pipe(
        map((records: UsattPlayerRecord[]) => {
          for (let i = 0; i < records.length; i++) {
            const record = records[i];
              record.membershipExpiration = dateUtils.convertFromString(record.membershipExpiration);
              record.dateOfBirth = dateUtils.convertFromString(record.dateOfBirth);
              record.lastLeaguePlayedDate = dateUtils.convertFromString(record.lastLeaguePlayedDate);
              record.lastTournamentPlayedDate = dateUtils.convertFromString(record.lastTournamentPlayedDate);
          }
          return records;
        })
      );
  }
}
