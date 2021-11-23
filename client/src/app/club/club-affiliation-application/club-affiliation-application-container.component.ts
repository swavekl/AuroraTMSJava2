import {Component, OnDestroy, OnInit} from '@angular/core';
import {ClubAffiliationApplicationService} from '../service/club-affiliation-application.service';
import {Observable, of, Subscription} from 'rxjs';
import {ClubAffiliationApplication} from '../model/club-affiliation-application.model';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {ActivatedRoute, Router} from '@angular/router';
import {createSelector} from '@ngrx/store';
import {first} from 'rxjs/operators';
import {ClubAffiliationApplicationStatus} from '../model/club-affiliation-application-status';

@Component({
  selector: 'app-club-affiliation-application-container',
  template: `
    <app-club-affiliation-application [clubAffiliationApplication]="clubAffiliationApplication$ | async"
    (saved)="onSaved($event)">
    </app-club-affiliation-application>
  `,
  styles: [
  ]
})
export class ClubAffiliationApplicationContainerComponent implements OnInit, OnDestroy {

  public clubAffiliationApplication$: Observable<ClubAffiliationApplication>;

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();
  private applicationId: number;
  private creating: boolean;

  constructor(private clubAffiliationApplicationService: ClubAffiliationApplicationService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService) {
    const routePath = this.activatedRoute.snapshot.routeConfig.path;
    this.creating = (routePath.indexOf('create') !== -1);
    const strId = this.activatedRoute.snapshot.params['id'] || 0;
    this.applicationId = Number(strId);
    this.setupProgressIndicator();
    this.loadApplication();
    }

  private setupProgressIndicator() {
    this.loading$ = this.clubAffiliationApplicationService.store.select(this.clubAffiliationApplicationService.selectors.selectLoading);

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }


  ngOnInit(): void {
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  onSaved(clubAffiliationApplication: ClubAffiliationApplication) {
    this.clubAffiliationApplicationService.upsert(clubAffiliationApplication)
      .pipe(first())
      .subscribe(
        (saved: ClubAffiliationApplication) => {
          // go back to list
          this.router.navigateByUrl('/club/affiliationlist');
        },
        (error: any) => {
          console.log('error on save', error);
        });
  }

  private loadApplication() {
    if (this.applicationId !== 0) {
      // load from server or cache
      const selector = createSelector(
        this.clubAffiliationApplicationService.selectors.selectEntityMap,
        (entityMap) => {
          return entityMap[this.applicationId];
        });
      // tournament information will not change just get it once
      this.clubAffiliationApplication$ = this.clubAffiliationApplicationService.store.select(selector);
      const subscription = this.clubAffiliationApplication$
        .subscribe((clubAffiliationApplication: ClubAffiliationApplication) => {
        if (!clubAffiliationApplication) {
          this.clubAffiliationApplicationService.getByKey(this.applicationId);
        } else {
          // clone it so that Angular template driven form can modify the values
          const applicationToEdit: ClubAffiliationApplication =  JSON.parse(JSON.stringify(clubAffiliationApplication));
          if (this.creating && this.applicationId !== 0) {
            applicationToEdit.id = null;
            applicationToEdit.affiliationExpirationDate = null;
            applicationToEdit.status = ClubAffiliationApplicationStatus.New;
            applicationToEdit.paymentId = null;
          }
          this.clubAffiliationApplication$ = of (applicationToEdit);
        }
      });
      this.subscriptions.add(subscription);
    } else {
      // new application
      this.clubAffiliationApplication$ = of (new ClubAffiliationApplication());
    }
  }
}
