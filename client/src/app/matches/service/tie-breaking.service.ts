import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {distinctUntilChanged, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {GroupTieBreakingInfo} from '../model/tie-breaking/group-tie-breaking-info.model';

@Injectable({
  providedIn: 'root'
})
export class TieBreakingService {

  // this service's base url
  private readonly baseUrl: string;

  // loading indicator just like in other services - used during load and save
  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean> = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());

  constructor(private http: HttpClient) {
    this.baseUrl = '/api/tiebreaking';
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  public rankAndAdvance(matchCardId: number): Observable<GroupTieBreakingInfo> {
    this.setLoading(true);
    const url = `${this.baseUrl}/${matchCardId}`;
    return this.http.get<GroupTieBreakingInfo>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          })
      );
  }

}
