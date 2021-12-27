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

  public download(matchCardId: number) {
    // this.setLoading(true);
    console.log('downloading match card id', matchCardId);
    const downloadAs = `matchcard_${matchCardId}.pdf`;
    const fullUrl = `https://${environment.baseServer}/api/matchcard/download/${matchCardId}`;
    this.httpClient.get(fullUrl, {
      responseType: 'blob'
    }).subscribe((blob: any) => {
      const a = document.createElement('a');
      const objectUrl = URL.createObjectURL(blob);
      a.href = objectUrl;
      a.download = downloadAs;
      a.click();
      URL.revokeObjectURL(objectUrl);
      // this.setLoading(false);
    });
  }
}
