import {Injectable, OnDestroy} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, Subscription} from 'rxjs';
import {distinctUntilChanged} from 'rxjs/operators';
import {environment} from '../../../environments/environment';
import * as printJS from 'print-js';
import {DownloadService} from '../../shared/download-service/download.service';
import {Download} from '../../shared/download-service/download';

/**
 * Service for downloading and priting individual match card or group of match cards for one event
 */
@Injectable({
  providedIn: 'root'
})
export class MatchCardPrinterService implements OnDestroy {

  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean>;

  download$: Observable<Download>;

  private subscriptions: Subscription = new Subscription();

  constructor(private httpClient: HttpClient,
              private downloadService: DownloadService) {
    this.loading$ = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  /**
   * Downloads a file and shows it in the browser's print dialog
   *
   * @param tournamentId
   * @param eventId
   * @param matchCardIds
   */
  public downloadAndPrint(tournamentId: number, eventId: number, matchCardIds: number[]) {
    this.setLoading(true);
    const firstMatchCardId = (matchCardIds.length === 1) ? matchCardIds[0] : 0;
    const downloadAs = (eventId != null && matchCardIds.length > 1)
      ? `matchcard_${tournamentId}_${eventId}.pdf`
      : `matchcard_${firstMatchCardId}.pdf`;
    const params = '?matchCardIds=' + matchCardIds.join(',');
    const pdfUrl = `https://${environment.baseServer}/api/matchcard/download` + params;
    this.download$ = this.downloadService.download(pdfUrl, downloadAs);
    const subscription = this.download$.subscribe(
      (downloadStatus: Download) => {
        if (downloadStatus.state === 'DONE') {
          this.setLoading(false);
          downloadStatus.content.text().then(base64 => {
            printJS({
              type: 'pdf',
              base64: true,
              printable: base64,
              onPrintDialogClose: function () {
                subscription.unsubscribe();
              }
            });
          });
        }
      });
    this.subscriptions.add(subscription);
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }
}
