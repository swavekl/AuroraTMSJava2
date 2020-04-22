import {Injectable} from '@angular/core';
import {AuthenticationService} from '../user/authentication.service';
import {map} from 'rxjs/operators';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Profile} from './profile';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  private profile$: Observable<Profile>;
  private baseUrl: string;

  constructor(private authenticationService: AuthenticationService,
              private http: HttpClient) {
    this.baseUrl = this.authenticationService.getFullUrl('/api/profiles');
  }

  getProfile(userId: string): Observable<Profile> {
    const url = `${this.baseUrl}/${userId}`;
    return this.http.get<Profile>(url)
      .pipe(
        map((response: Profile) => {
          return response;
        })
      );
  }

  /**
   * Updates profile
   * @param profile profile to update
   */
  updateProfile(profile: Profile): Observable<void> {
    const url = `${this.baseUrl}/${profile.userId}`;
    return this.http.put<void>(url, profile, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    });
  }
}
