import {Injectable} from '@angular/core';
import {distinctUntilChanged, finalize, map, tap} from 'rxjs/operators';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {BehaviorSubject, Observable} from 'rxjs';
import {Profile} from './profile';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  // this service's base url
  private readonly baseUrl: string;

  // loading indicator just like in other services - used during load and save
  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean> = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());

  constructor(private http: HttpClient) {
    this.baseUrl = '/api/profiles';
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  getProfile(userId: string): Observable<Profile> {
    this.setLoading(true);
    const url = `${this.baseUrl}/${userId}`;
    return this.http.get<Profile>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          })
      );
  }

  /**
   * Find profiles given filter expression e.g. firstName=John&lastName=Glen
   * @param searchCriteria expression
   */
  findProfiles(searchCriteria: any[]): Observable<Profile[]> {
    this.setLoading(true);
    let filter = '';
    for (const searchCriterion of searchCriteria) {
      filter += (filter.length === 0) ? '?' : '&';
      filter += searchCriterion.name + '=' + searchCriterion.value;
    }
    const url = `${this.baseUrl}search${filter}`;
    return this.http.get<Profile[]>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          })
      );
  }

  /**
   * Find profiles given filter expression e.g. firstName=John&lastName=Glen
   * @param searchCriteria expression
   */
  listProfiles(searchCriteria: any[]): Observable<ProfileListResponse> {
    this.setLoading(true);
    let filter = '';
    for (const searchCriterion of searchCriteria) {
      filter += (filter.length === 0) ? '?' : '&';
      filter += searchCriterion.name + '=' + searchCriterion.value;
    }
    const url = `${this.baseUrl}list${filter}`;
    return this.http.get<ProfileListResponse>(url)
      .pipe(
        tap({
          next: (response: ProfileListResponse) => {
            this.setLoading(false);
          },
          error: (error) => {
            this.setLoading(false);
            console.error(error);
          }
        })
      );
  }

  /**
   * Creates profile
   * @param profile profile to update
   */
  createProfile(profile: Profile): Observable<Profile> {
    this.setLoading(true);
    const url = `${this.baseUrl}`;
    return this.http.post<Profile>(url, profile, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          })
      );
  }

  /**
   * Updates profile
   * @param profile profile to update
   */
  updateProfile(profile: Profile): Observable<void> {
    this.setLoading(true);
    const url = `${this.baseUrl}/${profile.userId}`;
    return this.http.put<void>(url, profile, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          })
      );
  }

  unlockProfile(profile: Profile): Observable<Profile> {
    this.setLoading(true);
    const url = `${this.baseUrl}/${profile.userId}/unlock`;
    return this.http.put<Profile>(url, profile, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(
      tap({
        next: () => {
          this.setLoading(false);
        },
        error: err => {
          this.setLoading(false);
        }
      })
    );
  }

  getGroups(profileId: string): Observable<string[]> {
    this.setLoading(true);
    const url = `${this.baseUrl}/${profileId}/groups`;
    return this.http.get<string[]>(url, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(
      tap({
        next: () => {
          this.setLoading(false);
        },
        error: err => {
          this.setLoading(false);
        }
      })
    );
  }

  updateGroups(profileId: string, groups: string []) {
    this.setLoading(true);
    const url = `${this.baseUrl}/${profileId}/groups`;
    return this.http.put(url, groups, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(
      tap({
        next: () => {
          this.setLoading(false);
        },
        error: err => {
          this.setLoading(false);
        }
      })
    );
  }
}

export interface ProfileListResponse {
  profiles: Profile[];
  after: string;
}
