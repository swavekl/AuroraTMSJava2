import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable} from 'rxjs';
import {distinctUntilChanged} from 'rxjs/operators';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MatchCardPrinterService {

  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean>;

  constructor(private httpClient: HttpClient) {
    this.loading$ = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  public download(tournamentId: number, eventId: number, matchCardIds: number[]) {
    console.log('downloading match cards for ', matchCardIds);
    const firstMatchCardId = (matchCardIds.length === 1) ? matchCardIds[0] : 0;
    const downloadAs = (eventId != null && matchCardIds.length > 1)
      ? `matchcard_${tournamentId}_${eventId}.pdf`
      : `matchcard_${firstMatchCardId}.pdf`;
    console.log('downloadAs', downloadAs);
    const fullUrl = `https://${environment.baseServer}/api/matchcard/download`;
    this.httpClient.get(fullUrl, {
      params: {
        matchCardIds: matchCardIds
      },
      responseType: 'blob'
    }).subscribe((blob: any) => {
      const a = document.createElement('a');
      const objectUrl = URL.createObjectURL(blob);
      a.href = objectUrl;
      a.download = downloadAs;
      a.click();
      URL.revokeObjectURL(objectUrl);
    });
  }
}
