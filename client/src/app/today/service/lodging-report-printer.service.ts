import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
import { distinctUntilChanged } from 'rxjs/operators';
import printJS from 'print-js';
import { DownloadService } from '../../shared/download-service/download.service';
import { Download } from '../../shared/download-service/download';

/**
 * Service for downloading and printing the hospitality report for a tournament
 */
@Injectable({
  providedIn: 'root'
})
export class LodgingReportPrinterService implements OnDestroy {

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
   * Downloads the hospitality report file and passes it directly into the browser's print dialog
   *
   * @param tournamentId the id of the tournament
   */
  public downloadAndPrint(tournamentId: number) {
    this.setLoading(true);

    const downloadAs = `hospitality-list-${tournamentId}.pdf`;
    const pdfUrl = `/api/reports/lodging/${tournamentId}`;

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

          // Convert blob data to base64 locally on the client side
          this.convertToBase64AndPrint(downloadStatus, showBrowserPrintDialog);
        }
      },
      (error: any) => {
        this.setLoading(false);
        console.error('Error downloading hospitality report for printing', error);
      }
    );

    this.subscriptions.add(subscription);
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  /**
   * Converts the PDF blob stream into a base64 encoded payload and spins up the print dialog frame
   *
   * @param download download tracking data block
   * @param showBrowserPrintDialog callback executor
   * @private
   */
  private convertToBase64AndPrint(download: Download,
                                  showBrowserPrintDialog: (base64: any) => void) {
    // Define the FileReader which is able to read the contents of Blob
    const reader = new FileReader();

    // The magic always begins after the Blob is successfully loaded
    reader.onload = function (fileLoadedEvent) {
      // Since it contains the Data URI, we should remove the prefix and keep only Base64 string
      const base64WithHeader = fileLoadedEvent.target.result as string;
      const base64Trimmed = base64WithHeader.slice('data:application/pdf;base64,'.length);

      showBrowserPrintDialog(base64Trimmed);
    };

    // Since everything is set up, let’s read the Blob and store the result as Data URI
    reader.readAsDataURL(download.content);
  }
}
