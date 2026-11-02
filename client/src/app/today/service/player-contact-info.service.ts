import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {catchError, distinctUntilChanged, tap} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class PlayerContactInfoService {
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$: Observable<boolean> = this.loadingSubject.asObservable().pipe(distinctUntilChanged());

  constructor(private http: HttpClient) {
  }

  getForMatchCard(matchCardId: number, profileIds: string[]): Observable<Record<string, string | null>> {
    this.loadingSubject.next(true);
    return this.http.post<Record<string, string | null>>(`/api/profiles/contactinfo/${matchCardId}`, profileIds).pipe(
      tap({
        next: () => this.loadingSubject.next(false),
        error: () => this.loadingSubject.next(false)
      }),
      catchError(() => {
        this.loadingSubject.next(false);
        return of({});
      })
    );
  }
}
