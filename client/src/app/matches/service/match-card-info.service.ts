import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {distinctUntilChanged, tap} from 'rxjs/operators';
import {MatchCardInfo} from '../model/match-card-info.model';

@Injectable({
  providedIn: 'root'
})
export class MatchCardInfoService {

  // this service's base url
  private readonly baseUrl: string = '/api/matchcardinfos';

  // loading indicator just like in other services - used during load and save
  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean> = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());

  constructor(private http: HttpClient) {
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  public load(eventId: number, tournamentId: number = null, day: number = null): Observable<MatchCardInfo[]> {
    this.setLoading(true);
    let url = `${this.baseUrl}?eventId=${eventId}`;
    if (tournamentId != null && day != null) {
      url += `&tournamentId=${tournamentId}&day=${day}`;
    }
    return this.http.get<MatchCardInfo[]>(url)
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
