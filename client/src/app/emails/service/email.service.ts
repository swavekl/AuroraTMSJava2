import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {distinctUntilChanged, tap} from 'rxjs/operators';
import {BehaviorSubject, Observable, Subject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class EmailService {
  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean>;

  constructor(private httpClient: HttpClient) {
    this.loading$ = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  public getTournamentEmails(tournamentId: number): Observable<string[]> {
    this.setLoading(true);
    return this.httpClient.get(`/api/email/${tournamentId}`)
      .pipe(
        tap(
          (emails: string []) => {
            this.setLoading(false);
          },
          (error: any) => {
            console.error('error', error);
            this.setLoading(false);
          },
          () => {
          })
      );
  }
}
