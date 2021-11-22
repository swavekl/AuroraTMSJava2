import { Component, OnInit } from '@angular/core';
import {ClubAffiliationApplicationService} from '../service/club-affiliation-application.service';
import {Observable, of, Subscription} from 'rxjs';
import {ClubAffiliationApplication} from '../model/club-affiliation-application.model';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {ActivatedRoute} from '@angular/router';
import {createSelector} from '@ngrx/store';
import {first} from 'rxjs/operators';

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
export class ClubAffiliationApplicationContainerComponent implements OnInit {

  public clubAffiliationApplication$: Observable<ClubAffiliationApplication>;

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();
  private applicationId: number;

  constructor(private clubAffiliationApplicationService: ClubAffiliationApplicationService,
              private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService) {
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

  onSaved(clubAffiliationApplication: ClubAffiliationApplication) {
    this.clubAffiliationApplicationService.upsert(clubAffiliationApplication)
      .pipe(first())
      .subscribe((saved: ClubAffiliationApplication) => {
        console.log('saved application with id', saved.id);
        this.applicationId = saved.id;
      }, (error: any) => {
        console.log('error on save', error);
      });
  }

  private loadApplication() {
    if (this.applicationId !== 0) {
      // load from server or cache
      const selector = createSelector(
        this.clubAffiliationApplicationService.selectors.selectEntityMap,
        (entityMap) => {
          // clone it so that Angular template driven form can modify the values
          const app: ClubAffiliationApplication = entityMap[this.applicationId];
          return (app != null) ? JSON.parse(JSON.stringify(app)) : null;
        });
      // tournament information will not change just get it once
      this.clubAffiliationApplication$ = this.clubAffiliationApplicationService.store.select(selector);
      const subscription = this.clubAffiliationApplication$
        .subscribe((clubAffiliationApplication: ClubAffiliationApplication) => {
        if (!clubAffiliationApplication) {
          console.log('getting application from the server');
          this.clubAffiliationApplicationService.getByKey(this.applicationId);
        } else {
          console.log('got the affiliation applicaiton ', clubAffiliationApplication);
        }
      });
      this.subscriptions.add(subscription);
    } else {
      // new application
      this.clubAffiliationApplication$ = of (new ClubAffiliationApplication());
    }
  }
}
