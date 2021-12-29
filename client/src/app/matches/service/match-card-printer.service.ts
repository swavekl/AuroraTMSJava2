import {Injectable, OnDestroy} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, Subscription} from 'rxjs';
import {distinctUntilChanged} from 'rxjs/operators';
import {environment} from '../../../environments/environment';
import * as printJS from 'print-js';
import {DownloadService} from '../../shared/download-service/download.service';
import {Download} from '../../shared/download-service/download';

/**
 * Service for downloading and printing individual match card or group of match cards for one event
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
    let params = '?matchCardIds=' + matchCardIds.join(',');
    const downloadAsBase64 = true;
    params += `&base64=${downloadAsBase64}`;
    const pdfUrl = `https://${environment.baseServer}/api/matchcard/download` + params;
    this.download$ = this.downloadService.download(pdfUrl, downloadAs);
    const subscription = this.download$.subscribe(
      (downloadStatus: Download) => {
        if (downloadStatus.state === 'DONE') {
          this.setLoading(false);
          subscription.unsubscribe();

          const showBrowserPrintDialog = function (base64: any) {
            printJS({
              type: 'pdf',
              base64: true,
              printable: base64,
              onPrintDialogClose: function () {
                console.log('after print dialog close');
              }
            });
          };
          if (downloadAsBase64) {
            // convert to base64 on the server
            downloadStatus.content.text().then(base64 => {
              // console.log ('base64  is: ', base64);
              showBrowserPrintDialog(base64);
            });
          } else {
            // convert to base 64 on the client
            this.convertToBase64AndPrint(downloadStatus, showBrowserPrintDialog);
          }
        }
      });
    this.subscriptions.add(subscription);
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  /**
   * Converts the PDF stream to base64 string and starts print dialog
   *
   * @param download download result
   * @param showBrowserPrintDialog
   * @private
   */
  private convertToBase64AndPrint(download: Download,
                                  showBrowserPrintDialog: (base64: any) => void) {
    // Define the FileReader which is able to read the contents of Blob
    const reader = new FileReader();

    // The magic always begins after the Blob is successfully loaded
    reader.onload = function (fileLoadedEvent) {
      // Since it contains the Data URI, we should remove the prefix and keep only Base64 string
      const base64WithHeader = fileLoadedEvent.target.result;
      const base64Trimmed = base64WithHeader.slice('data:application/pdf;base64,'.length);
      // console.log ('base64', base64Trimmed);
      showBrowserPrintDialog(base64Trimmed);
    };

    // Since everything is set up, let’s read the Blob and store the result as Data URI
    reader.readAsDataURL(download.content);
  }
}
