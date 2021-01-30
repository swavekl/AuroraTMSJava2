import {Injectable} from '@angular/core';
import {AuthenticationService} from '../user/authentication.service';
import {distinctUntilChanged, finalize, map} from 'rxjs/operators';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {BehaviorSubject, Observable} from 'rxjs';
import {Profile} from './profile';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  private profile$: Observable<Profile>;
  private baseUrl: string;
  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean> = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());

  constructor(private authenticationService: AuthenticationService,
              private http: HttpClient) {
    this.baseUrl = this.authenticationService.getFullUrl('/api/profiles');
  }

  getProfile(userId: string): Observable<Profile> {
    this.indicatorSubject$.next(true);
    const url = `${this.baseUrl}/${userId}`;
    return this.http.get<Profile>(url)
      .pipe(
        map((response: Profile) => {
          return response;
        }),
        finalize(() => this.indicatorSubject$.next(false))
      );
  }

  /**
   * Find profiles given filter expression e.g. firstName=John&lastName=Glen
   * @param filter expression
   */
  findProfiles(searchCriteria: any[]): Observable<Profile[]> {
    let filter = '';
    for (const searchCriterion of searchCriteria) {
      filter += (filter.length === 0) ? '?' : '&';
      filter += searchCriterion.name + '=' + searchCriterion.value;
    }
    const url = `${this.baseUrl}search${filter}`;
    return this.http.get<Profile[]>(url)
      .pipe(
        map((response: Profile[]) => {
          return response;
        })
      );
  }

  /**
   * Updates profile
   * @param profile profile to update
   */
  updateProfile(profile: Profile): Observable<void> {
    this.indicatorSubject$.next(true);
    const url = `${this.baseUrl}/${profile.userId}`;
    return this.http.put<void>(url, profile, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    })
      .pipe(finalize(() => this.indicatorSubject$.next(false)));
  }
}
