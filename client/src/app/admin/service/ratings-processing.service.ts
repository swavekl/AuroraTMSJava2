import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {distinctUntilChanged, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {RatingsProcessorStatus} from '../model/ratings-processor-status';

@Injectable({
  providedIn: 'root'
})
export class RatingsProcessingService {

  // this service's base url
  private readonly baseUrl: string;

  // loading indicator just like in other services - used during load and save
  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean> = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());

  constructor(private http: HttpClient) {
    this.baseUrl = '/api/ratingsprocessing';
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  public process(ratingsFile: string): Observable<RatingsProcessorStatus> {
    this.setLoading(true);
    const url = `${this.baseUrl}?ratingsFile=${ratingsFile}`;
    return this.http.get<RatingsProcessorStatus>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          })
      );
  }

  public getStatus(): Observable<RatingsProcessorStatus> {
    const url = `${this.baseUrl}/status`;
    return this.http.get<RatingsProcessorStatus>(url);
  }
}
