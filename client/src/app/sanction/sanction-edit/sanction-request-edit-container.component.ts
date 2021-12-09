import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Observable, of, Subscription} from 'rxjs';
import {first} from 'rxjs/operators';
import {createSelector} from '@ngrx/store';
import {SanctionRequestService} from '../service/sanction-request.service';
import {SanctionRequest, SanctionRequestStatus} from '../model/sanction-request.model';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';

@Component({
  selector: 'app-sanction-edit-container',
  template: `
    <app-sanction-request-edit [sanctionRequest]="sanctionRequest$ | async"
    (saved)="onSave($event)" (canceled)="onCancel($event)">
    </app-sanction-request-edit>
  `,
  styles: [
  ]
})
export class SanctionRequestEditContainerComponent implements OnInit, OnDestroy {
  public sanctionRequest$: Observable<SanctionRequest>;

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();
  private sanctionRequestId: number;
  private creating: boolean;

  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService,
              private sanctionRequestService: SanctionRequestService) {
    const routePath = this.activatedRoute.snapshot.routeConfig.path;
    this.creating = (routePath.indexOf('create') !== -1);
    const strId = this.activatedRoute.snapshot.params['id'] || 0;
    this.sanctionRequestId = Number(strId);
    this.setupProgressIndicator();
    this.loadSanctionRequest();
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    this.loading$ = this.sanctionRequestService.store.select(this.sanctionRequestService.selectors.selectLoading);

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  /**
   *
   * @private
   */
  private loadSanctionRequest() {
    if (this.sanctionRequestId !== 0) {
      const selector = createSelector(
        this.sanctionRequestService.selectors.selectEntityMap,
        (entityMap) => {
          return entityMap[this.sanctionRequestId];
        });

      this.sanctionRequest$ = this.sanctionRequestService.store.select(selector);
      const subscription = this.sanctionRequest$.subscribe((sanctionRequest: SanctionRequest) => {
        if (!sanctionRequest) {
          this.sanctionRequestService.getByKey(this.sanctionRequestId);
        } else {
          // clone it
          const sanctionRequestToEdit: SanctionRequest = JSON.parse(JSON.stringify(sanctionRequest));
          // if making a copy of existing one
          if (this.creating && this.sanctionRequestId !== 0) {
            sanctionRequestToEdit.id = null;
            sanctionRequestToEdit.status = SanctionRequestStatus.New;
            sanctionRequestToEdit.requestDate = new Date();
          }
          this.sanctionRequest$ = of(sanctionRequestToEdit);
        }
      });

      this.subscriptions.add(subscription);

    } else {
      this.sanctionRequest$ = of(new SanctionRequest());
    }
  }

  public onSave(sanctionRequest: SanctionRequest) {
      this.sanctionRequestService.upsert(sanctionRequest)
        .pipe(first())
        .subscribe(
          (savedSanctionRequest: SanctionRequest) => {
            this.sanctionRequestId = savedSanctionRequest.id;
            this.goBackToList();
          }, (error: any) => {
            console.log('error on save', error);
          });
    }

    onCancel($event: any) {
      this.goBackToList();
    }

  private goBackToList() {
      // go back to list
      this.router.navigateByUrl('/sanction/list');
    }
}
