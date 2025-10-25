import {Component, OnDestroy, OnInit} from '@angular/core';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {Observable, of, Subscription, takeWhile, tap, timer} from 'rxjs';
import {RatingsProcessingService} from '../service/ratings-processing.service';
import {catchError, first, map, switchMap} from 'rxjs/operators';
import {RatingsProcessorStatus} from '../model/ratings-processor-status';
import {MembershipsProcessingService} from '../service/memberships-processing.service';
import {MembershipsProcessorStatus} from '../model/memberhips-processor-status';
import {ErrorMessagePopupService} from '../../shared/error-message-dialog/error-message-popup.service';

@Component({
    selector: 'app-admin-ratings-container',
    template: `
    <app-admin-ratings [processing]="continueCheckingStatus"
                       [ratingsProcessorStatus]="status$ | async"
                       (uploaded)="onRatingsFileUploaded($event)"
                       [membershipsProcessorStatus]="membershipsStatus$ | async"
                       (membershipsUploaded)="onMembershipsFileUploaded($event)">
    </app-admin-ratings>
  `,
    styles: [],
    standalone: false
})
export class AdminRatingsContainerComponent implements OnInit, OnDestroy {

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();

  status$: Observable<RatingsProcessorStatus>;
  membershipsStatus$: Observable<MembershipsProcessorStatus>;
  continueCheckingStatus: boolean;

  constructor(private ratingsProcessingService: RatingsProcessingService,
              private membershipsProcessingService: MembershipsProcessingService,
              private linearProgressBarService: LinearProgressBarService,
              private errorMessagePopupService: ErrorMessagePopupService) {
    this.setupProgressIndicator();
    this.status$ = of(new RatingsProcessorStatus());
    this.membershipsStatus$ = of(new MembershipsProcessorStatus());
    this.continueCheckingStatus = false;
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  ngOnInit(): void {
    // throw new Error('Method not implemented.');
  }

  private setupProgressIndicator() {
    this.loading$ = this.ratingsProcessingService.loading$;

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  onRatingsFileUploaded(uploadedFileURL: string) {
    this.continueCheckingStatus = true;
    this.ratingsProcessingService.process(uploadedFileURL)
      .pipe(first(),
        map((response: RatingsProcessorStatus) => {
          return of(response);
        }))
      .subscribe();

    const source = timer(0, 5 * 1000);
    const subscription = source.pipe(
      switchMap(() =>
        this.ratingsProcessingService.getStatus()
          .pipe(
            tap((status: RatingsProcessorStatus) => {
              this.status$ = of(status);
              this.continueCheckingStatus = (status.phase !== 'Finished');
            }),
            catchError(error => {
              // handle error case
              this.continueCheckingStatus = false;
              const message = error.error?.error ?? 'Error';
              this.errorMessagePopupService.showError(message);
              return of({phase: 'Error'} as RatingsProcessorStatus);
            })
          )
      ),
      takeWhile(() => this.continueCheckingStatus)
    ).subscribe();

    this.subscriptions.add(subscription);

  }

  onMembershipsFileUploaded(uploadedFileURL: string) {
    this.continueCheckingStatus = true;
      this.membershipsProcessingService.process(uploadedFileURL)
        .pipe(first())
        .subscribe({
          next: (response: MembershipsProcessorStatus) => {
            this.membershipsStatus$ = of (response)
            return of(response);
          },
          error: (error: any) => {
            this.continueCheckingStatus = false;
            const message = error.error?.message ?? error.message;
            this.errorMessagePopupService.showError(message);
          }
        });

    const source = timer(0, 3 * 1000);
    const subscription = source.pipe(
      switchMap(() =>
        this.membershipsProcessingService.getStatus()
          .pipe(
            tap((status: MembershipsProcessorStatus) => {
              this.membershipsStatus$ = of(status);
              this.continueCheckingStatus = (status.phase !== 'Finished');
            }),
            catchError(error => {
              // handle error case
              this.continueCheckingStatus = false;
              const message = error.error?.error ?? 'Error';
              this.errorMessagePopupService.showError(message);
              return of({phase: 'Error'} as MembershipsProcessorStatus);
            })
          )
      ),
      takeWhile(() => this.continueCheckingStatus)
    ).subscribe();
    this.subscriptions.add(subscription);
  }
}
