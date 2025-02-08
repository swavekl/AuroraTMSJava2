import {Component, OnDestroy} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {first, map, tap} from 'rxjs/operators';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {EmailSenderService} from '../service/email-sender.service';
import {EmailServerConfiguration} from '../model/email-server-configuration.model';
import {EmailServerConfigurationService} from '../service/email-server-configuration.service';
import {AuthenticationService} from '../../user/authentication.service';
import {createSelector} from '@ngrx/store';
import {CampaignInitData} from '../email-add-dialog/email-add-dialog.component';

@Component({
  selector: 'app-email-container',
  template: `
      <app-email-campaign-list
              [tournamentName]="tournamentName"
              [tournamentId]="tournamentId"
              [emailAddresses]="emailAddresses"
              [emailServerConfiguration]="emailServerConfiguration$ | async"
              (eventEmitter)="onEvent($event)"
              (addCampaign)="onAddCampaign($event)"
              (emailConfigSave)="onEmailConfigSave($event)">
      </app-email-campaign-list>
  `,
  styles: []
})
export class EmailCampaignListContainerComponent implements OnDestroy {

  tournamentId: number;
  tournamentName: string;
  emailAddresses: string;

  emailServerConfiguration$: Observable<EmailServerConfiguration>;

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();

  constructor(private emailService: EmailSenderService,
              private emailServerConfigurationService: EmailServerConfigurationService,
              private authenticationService: AuthenticationService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    this.tournamentName = this.activatedRoute.snapshot.params['tournamentName'] || 'N/A';
    this.emailAddresses = '';
    this.setupProgressIndicator();
    this.loadEmailServerConfiguration();
  }

  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest([
        this.emailService.loading$,
        this.emailServerConfigurationService.store.select(this.emailServerConfigurationService.selectors.selectLoading)
      ],
      (emailsLoading: boolean, emailConfigurationLoading: boolean) => {
        return emailsLoading || emailConfigurationLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  onEvent($event: string) {
    if ($event === 'getemails') {
      // this.emailService.getTournamentEmails(this.tournamentId)
      //   .pipe(first())
      //   .subscribe(
      //     (emails: string[]) => {
      //       const emailsCommaSeparated = emails.join(',');
      //       // console.log('emails', emailsCommaSeparated);
      //       this.emailAddresses = emailsCommaSeparated;
      //     });
    } else if ($event === 'back') {
      const returnUrl: string = `/ui/tournamentsconfig`;
      this.router.navigateByUrl(returnUrl);
    }
  }

  onAddCampaign(campaignInitData: CampaignInitData) {
    const extras = {
      state: {
        campaignInitData: campaignInitData
      }
    };
    const url = `/ui/email/emailcampaign/create/${this.tournamentId}/0`;
    this.router.navigateByUrl(url, extras);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private loadEmailServerConfiguration() {
    const userProfileId = this.authenticationService.getCurrentUserProfileId();
    const selector = createSelector(
      this.emailServerConfigurationService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[userProfileId];
      });
    const localConfiguration$ = this.emailServerConfigurationService.store.select(selector);
    const subscription = localConfiguration$.pipe(
      first(),
      map((emailServerConfiguration: EmailServerConfiguration): void => {
          if (emailServerConfiguration == null) {
            const subscription2 = this.emailServerConfigurationService.getByKey(userProfileId)
              .subscribe((emailServerConfiguration2: EmailServerConfiguration) => {
                this.emailServerConfiguration$ = of(emailServerConfiguration2);
              }, error => {
                // not found - create new one
                const newConfiguration: EmailServerConfiguration = {
                  profileId: userProfileId,
                  serverHost: null,
                  serverPort: 25,
                  userId: null,
                  password: null
                };
                this.emailServerConfiguration$ = of(newConfiguration);
              }, () => {
              });
            this.subscriptions.add(subscription2);
          } else {
            // console.log(`Got email server config in cache`, emailServerConfiguration);
            this.emailServerConfiguration$ = of(emailServerConfiguration);
          }
        }
      )).subscribe();
    this.subscriptions.add(subscription);
  }

  onEmailConfigSave(config: EmailServerConfiguration) {
    this.emailServerConfigurationService.upsert(config)
      .pipe(first())
      .subscribe((savedConfig: EmailServerConfiguration) => {
        this.emailServerConfiguration$ = of(savedConfig);
      }, (error) => {
        console.log('Error saving configuration', error);
      }, () => {
        // console.log('Completed save');
      });
  }
}
