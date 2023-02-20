import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Observable, of, Subscription} from 'rxjs';
import {first} from 'rxjs/operators';
import {createSelector} from '@ngrx/store';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {InsuranceRequest} from '../model/insurance-request.model';
import {InsuranceRequestStatus} from '../model/insurance-request-status';
import {InsuranceRequestService} from '../service/insurance-request.service';

@Component({
  selector: 'app-insurance-container',
  template: `
    <app-insurance [insuranceRequest]="insuranceRequest$ | async"
                   (saved)="onSave($event)" (canceled)="onCancel($event)">
    </app-insurance>
  `,
  styles: []
})
export class InsuranceContainerComponent implements OnInit, OnDestroy {
  public insuranceRequest$: Observable<InsuranceRequest>;

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();
  private insuranceRequestId: number;
  private creating: boolean;

  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService,
              private insuranceRequestService: InsuranceRequestService) {
    const routePath = this.activatedRoute.snapshot.routeConfig.path;
    this.creating = (routePath.indexOf('create') !== -1);
    const strId = this.activatedRoute.snapshot.params['id'] || 0;
    this.insuranceRequestId = Number(strId);
    this.setupProgressIndicator();
    this.loadInsuranceRequest();
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    this.loading$ = this.insuranceRequestService.store.select(this.insuranceRequestService.selectors.selectLoading);

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  /**
   *
   * @private
   */
  private loadInsuranceRequest() {
    if (this.insuranceRequestId !== 0) {
      const selector = createSelector(
        this.insuranceRequestService.selectors.selectEntityMap,
        (entityMap) => {
          return entityMap[this.insuranceRequestId];
        });

      this.insuranceRequest$ = this.insuranceRequestService.store.select(selector);
      const subscription = this.insuranceRequest$.subscribe((insuranceRequest: InsuranceRequest) => {
        if (!insuranceRequest) {
          this.insuranceRequestService.getByKey(this.insuranceRequestId);
        } else {
          // clone it
          const insuranceRequestToEdit: InsuranceRequest = JSON.parse(JSON.stringify(insuranceRequest));
          // if making a copy of existing one
          if (this.creating && this.insuranceRequestId !== 0) {
            insuranceRequestToEdit.id = null;
            insuranceRequestToEdit.status = InsuranceRequestStatus.New;
            insuranceRequestToEdit.requestDate = new Date();
            insuranceRequestToEdit.certificateUrl = null;
            insuranceRequestToEdit.additionalInsuredAgreementUrl = null;
          }
          this.insuranceRequest$ = of(insuranceRequestToEdit);
        }
      });

      this.subscriptions.add(subscription);

    } else {
      this.insuranceRequest$ = of(new InsuranceRequest());
    }
  }

  public onSave(insuranceRequest: InsuranceRequest) {
    this.insuranceRequestService.upsert(insuranceRequest)
      .pipe(first())
      .subscribe(
        (savedInsuranceRequest: InsuranceRequest) => {
          this.insuranceRequestId = savedInsuranceRequest.id;
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
    this.router.navigateByUrl('/ui/insurance/list');
  }

}
