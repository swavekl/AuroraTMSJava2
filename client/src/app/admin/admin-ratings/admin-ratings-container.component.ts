import {Component, OnDestroy, OnInit} from '@angular/core';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {Observable, of, Subscription, takeWhile, tap, timer} from 'rxjs';
import {RatingsProcessingService} from '../service/ratings-processing.service';
import {first, map, switchMap} from 'rxjs/operators';
import {RatingsProcessorStatus} from '../model/ratings-processor-status';

@Component({
  selector: 'app-admin-ratings-container',
  template: `
    <app-admin-ratings [processing]="continueCheckingStatus"
                       [ratingsProcessorStatus]="status$ | async"
                       (uploaded)="onRatingsFileUploaded($event)">
    </app-admin-ratings>
  `,
  styles: []
})
export class AdminRatingsContainerComponent implements OnInit, OnDestroy {

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();

  status$: Observable<RatingsProcessorStatus>;
  continueCheckingStatus: boolean;

  constructor(private ratingsProcessingService: RatingsProcessingService,
              private linearProgressBarService: LinearProgressBarService) {
    this.setupProgressIndicator();
    this.status$ = of(new RatingsProcessorStatus());
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
      takeWhile(value => {
          if (this.continueCheckingStatus) {
            this.ratingsProcessingService.getStatus()
              .pipe(first(),
                tap((status: RatingsProcessorStatus) => {
                  this.status$ = of (status);
                  this.continueCheckingStatus = (status.phase !== 'Finished');
                }))
              .subscribe();
          }
          return this.continueCheckingStatus === true;
        }
      )).subscribe();
    this.subscriptions.add(subscription);

  }


}
