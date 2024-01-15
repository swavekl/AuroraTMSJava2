import {Component, OnDestroy} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {first} from 'rxjs/operators';
import {createSelector} from '@ngrx/store';

import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {EmailCampaignService} from '../service/email-campaign.service';
import {EmailCampaign} from '../model/email-campaign.model';
import {ErrorMessagePopupService} from '../../shared/error-message-dialog/error-message-popup.service';

@Component({
  selector: 'app-email-campaign-edit-container',
  template: `
      <app-email-campaign-edit
              [tournamentName]="tournamentName"
              [emailCampaign]="emailCampaign$ | async"
              (eventEmitter)="onEvent($event)">
      </app-email-campaign-edit>
  `,
  styles: []
})
export class EmailCampaignEditContainerComponent implements OnDestroy {

  private emailCampaignId: number;
  public emailCampaign$: Observable<EmailCampaign>;
  tournamentName: string;
  private creating: boolean;
  private returnUrl: string;

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();

  constructor(private emailCampaignService: EmailCampaignService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService,
              private errorMessagePopupService: ErrorMessagePopupService) {
    const strEmailCampaignId = this.activatedRoute.snapshot.params['emailCampaignId'] || 0;
    this.emailCampaignId = Number(strEmailCampaignId);
    const routePath = this.activatedRoute.snapshot.routeConfig.path;
    this.creating = (routePath.indexOf('create') !== -1);
    this.setupProgressIndicator();
    this.loadEmailCampaign();
  }

  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest([
        this.emailCampaignService.store.select(this.emailCampaignService.selectors.selectLoading)
      ],
      (emailCampaignLoading: boolean) => {
        return emailCampaignLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadEmailCampaign() {
    // load when editing or cloning from another campaign
    const load = (!this.creating || (this.creating && this.emailCampaignId != 0));
    if (load) {
      // load from server or cache
      const selector = createSelector(
        this.emailCampaignService.selectors.selectEntityMap,
        (entityMap) => {
          return entityMap[this.emailCampaignId];
        });
      // hook up selector
      this.emailCampaign$ = this.emailCampaignService.store.select(selector);

      // load
      const subscription = this.emailCampaign$
        .subscribe((emailCampaign: EmailCampaign) => {
          if (!emailCampaign) {
            // not in cache, load it
            this.emailCampaignService.getByKey(this.emailCampaignId);
          } else {
            // loaded successfully
            // clone it so that Angular template driven form can modify the values
            const emailCampaignToEdit: EmailCampaign = JSON.parse(JSON.stringify(emailCampaign));
            if (this.creating && this.emailCampaignId !== 0) {
              emailCampaignToEdit.id = null;
              emailCampaignToEdit.name = emailCampaignToEdit.name + " Copy";
            }
            this.emailCampaign$ = of(emailCampaignToEdit);
          }
        });
      this.subscriptions.add(subscription);
    } else {
      // new campaign from scratch
      this.emailCampaign$ = of(new EmailCampaign());
    }
  }

  onEvent($event: any) {
    const action = $event.action;
    if (action === 'save') {
      const emailCampaign: EmailCampaign = $event.value;
      this.emailCampaignService.upsert(emailCampaign)
        .pipe(first())
        .subscribe({next: (savedCampaign: EmailCampaign) => {
          this.back();
        }, error: (error: any) => {
          const errorMessage: string = (error.error?.message) ?? error.message;
          this.errorMessagePopupService.showError(errorMessage, null, null, 'Error Saving Campaign');
        },
        complete: () => {}});
    } else if (action === 'sendemails') {
      console.log('sending emails...');
      this.back();
    } else {
      this.back();
    }
  }

  back() {
    // this.router.navigateByUrl('/ui/email/')
    history.back();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

}
