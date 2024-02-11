import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Registration} from '../model/registration.model';
import {BehaviorSubject, Observable} from 'rxjs';
import {distinctUntilChanged, tap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class RegistrationService {

  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean>;

  constructor(private httpClient: HttpClient) {
    this.loading$ = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  load (profileId: string): Observable<Registration[]> {
    this.setLoading(true);
    return this.httpClient.get(`/api/registrations/${profileId}`)
      .pipe(
        tap({
          next: (registrations: Registration[]) => {
            this.setLoading(false);
          },
          error: error => {
            this.setLoading(false);
            console.log('Registrations Error:', error)
          },
          complete: () => {}
        })
    );
  }
}
