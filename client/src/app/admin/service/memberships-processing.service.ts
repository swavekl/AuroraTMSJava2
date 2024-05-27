import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {distinctUntilChanged, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {MembershipsProcessorStatus} from '../model/memberhips-processor-status';

@Injectable({
  providedIn: 'root'
})
export class MembershipsProcessingService {

  // this service's base url
  private readonly baseUrl: string;

  // loading indicator just like in other services - used during load and save
  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean> = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());

  constructor(private http: HttpClient) {
    this.baseUrl = '/api/membershipsprocessing';
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  public process(ratingsFile: string): Observable<MembershipsProcessorStatus> {
    this.setLoading(true);
    const url = `${this.baseUrl}?membershipsFile=${ratingsFile}`;
    return this.http.get<MembershipsProcessorStatus>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
            console.error('error ', error);
          })
      );
  }

  public getStatus(): Observable<MembershipsProcessorStatus> {
    const url = `${this.baseUrl}/status`;
    return this.http.get<MembershipsProcessorStatus>(url);
  }
}
