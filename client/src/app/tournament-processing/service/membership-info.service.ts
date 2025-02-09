import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {distinctUntilChanged, tap} from 'rxjs/operators';
import {MembershipInfo} from '../model/membership-info.model';

@Injectable({
  providedIn: 'root'
})
export class MembershipInfoService {

  // loading indicator just like in other services - used during load and save
  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean> = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());

  private readonly baseUrl: string = '/api/membershipinfo';

  constructor(private http: HttpClient) {
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  load(tournamentId: number): Observable<MembershipInfo[]> {
    this.setLoading(true);
    const url = `${this.baseUrl}/list/${tournamentId}`;
    return this.http.get<MembershipInfo[]>(url)
      .pipe(
        tap({
          next: (response: MembershipInfo[]) => {
            this.setLoading(false);
          },
          error: (error) => {
            this.setLoading(false);
            console.error(error);
          }
        })
      );
  }

  contactPlayers(tournamentId: number, membershipInfos: MembershipInfo[]): Observable<any> {
    this.setLoading(true);
    const url = `${this.baseUrl}/contactplayers/${tournamentId}`;
    return this.http.post(url, membershipInfos)
      .pipe(
        tap({
          next: (response: any) => {
            this.setLoading(false);
          },
          error: (error) => {
            this.setLoading(false);
            console.error(error);
          }
        })
      );
  }
}
