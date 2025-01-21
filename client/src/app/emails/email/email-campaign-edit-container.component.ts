import {Component, OnDestroy} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {first, map, switchMap} from 'rxjs/operators';
import {createSelector} from '@ngrx/store';

import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {EmailCampaignService} from '../service/email-campaign.service';
import {EmailCampaign, Recipient} from '../model/email-campaign.model';
import {ErrorMessagePopupService} from '../../shared/error-message-dialog/error-message-popup.service';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {EmailSenderService} from '../service/email-sender.service';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {error} from '@angular/compiler-cli/src/transformers/util';

@Component({
  selector: 'app-email-campaign-edit-container',
  template: `
      <app-email-campaign-edit
              [tournamentName]="tournamentName"
              [emailCampaign]="emailCampaign$ | async"
              [tournamentEvents]="tournamentEvents$ | async"
              [filteredRecipients]="filteredRecipients$ | async"
              (eventEmitter)="onEvent($event)">
      </app-email-campaign-edit>
  `,
  styles: []
})
export class EmailCampaignEditContainerComponent implements OnDestroy {

  private emailCampaignId: number;
  private tournamentId: number;
  public tournamentName: string;

  public emailCampaign$: Observable<EmailCampaign>;
  public tournamentEvents$: Observable<TournamentEvent[]>;
  public filteredRecipients$: Observable<Recipient[]>;
  private totalFilteredRecipients: number;

  private creating: boolean;
  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();

  constructor(private emailCampaignService: EmailCampaignService,
              private emailSenderService: EmailSenderService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService,
              private errorMessagePopupService: ErrorMessagePopupService,
              private dialog: MatDialog) {
    const strEmailCampaignId = this.activatedRoute.snapshot.params['emailCampaignId'] || 0;
    this.emailCampaignId = Number(strEmailCampaignId);
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    const routePath = this.activatedRoute.snapshot.routeConfig.path;
    this.creating = (routePath.indexOf('create') !== -1);
    this.setupProgressIndicator();
    this.loadEmailCampaign();
    this.loadTournamentEvents(this.tournamentId);
  }

  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest([
        this.emailCampaignService.store.select(this.emailCampaignService.selectors.selectLoading),
        this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
        this.emailSenderService.loading$
      ],
      (emailCampaignLoading: boolean, tournamentEventsLoading: boolean, emailServiceLoading: boolean) => {
        return emailCampaignLoading || tournamentEventsLoading || emailServiceLoading;
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
            if (emailCampaignToEdit?.stateFilters?.length > 0) {
              emailCampaignToEdit.allRecipients = true;
            }
            this.emailCampaign$ = of(emailCampaignToEdit);
            if (emailCampaignToEdit.recipientFilters?.length > 0 || emailCampaignToEdit?.stateFilters?.length > 0) {
              const filterConfiguration = {
                recipientFilters: emailCampaignToEdit.recipientFilters,
                removedRecipients: emailCampaignToEdit.removedRecipients,
                allRecipients: emailCampaignToEdit?.allRecipients,
                excludeRegistered: emailCampaignToEdit?.excludeRegistered,
                stateFilters: emailCampaignToEdit?.stateFilters,
                uploadedRecipientsFile: emailCampaignToEdit?.uploadedRecipientsFile,
                includeUploadedRecipients: emailCampaignToEdit?.includeUploadedRecipients
              };
              this.loadRecipients(filterConfiguration);
            }
          }
        });
      this.subscriptions.add(subscription);
    } else {
      // new campaign from scratch
      this.emailCampaign$ = of(new EmailCampaign());
    }
  }

  private loadTournamentEvents(tournamentId: number) {
    this.tournamentEvents$ = this.tournamentEventConfigService.store.select(
      this.tournamentEventConfigService.selectors.selectEntities);
    // load them - they will surface via this selector
    this.tournamentEventConfigService.loadTournamentEvents(tournamentId);
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
    } else if (action === 'filter') {
      // console.log('recipientFilters', $event.recipientFilters);
      const filterConfiguration = {
        recipientFilters: $event.recipientFilters,
        removedRecipients: $event.removedRecipients,
        allRecipients: $event?.allRecipients,
        excludeRegistered: $event?.excludeRegistered,
        stateFilters: $event?.stateFilters,
        uploadedRecipientsFile: $event?.uploadedRecipientsFile,
        includeUploadedRecipients: $event?.includeUploadedRecipients
      };
      this.loadRecipients(filterConfiguration);
    } else if (action === 'sendemails') {
      const emailCampaign: EmailCampaign = $event.value;
      this.saveAndSendEmailCampaign(this.tournamentId, emailCampaign, false);
      // this.back();
    } else if (action === 'sendtestemail') {
      const emailCampaign: EmailCampaign = $event.value;
      this.saveAndSendEmailCampaign(this.tournamentId, emailCampaign, true);
    } else {
      this.back();
    }
  }

  back() {
    history.back();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private loadRecipients(filterConfiguration: any) {
    this.emailSenderService.getRecipientEmails(this.tournamentId, filterConfiguration)
      .pipe(
        switchMap(
          (recipients: Recipient []) => {
            recipients.sort((recipient1: Recipient, recipient2: Recipient) => {
              const fullName1 = `${recipient1.lastName}, ${recipient1.firstName}`;
              const fullName2 = `${recipient2.lastName}, ${recipient2.firstName}`;
              return fullName1.localeCompare(fullName2)
            });

            return of(recipients);
          }))
      .subscribe({
          next: (filteredRecipients: Recipient[]) => {
            this.filteredRecipients$ = of(filteredRecipients);
            this.totalFilteredRecipients = filteredRecipients.length;
          },
          error: (error: any) => {
            console.log('error', error);
          },
          complete: () => {

          }
        }
      );
  }

  /**
   * Saves campaign and sends it
   * @param tournamentId
   * @param emailCampaign
   * @param sendTestEmail
   * @private
   */
  private saveAndSendEmailCampaign(tournamentId: number, emailCampaign: EmailCampaign, sendTestEmail: boolean) {
    this.emailCampaignService.upsert(emailCampaign)
      .pipe(
        first(),
        switchMap((savedCampaign: EmailCampaign): Observable<any> => {
          this.doSendCampaign(tournamentId, savedCampaign, sendTestEmail);
          return of('');
        })
      )
      .subscribe({
        next: (result: string) => {
        },
        error: (error: any) => {
          const errorMessage: string = (error.error?.message) ?? error.message;
          this.errorMessagePopupService.showError(errorMessage, null, null, 'Error Saving Campaign');
        },
        complete: () => {
        }
      });
  }

  private doSendCampaign(tournamentId: number, emailCampaign: EmailCampaign, sendTestEmail: boolean): void {
    this.emailSenderService.sendCampaign(tournamentId, emailCampaign, sendTestEmail)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          const timeToSend: number = Math.floor((this.totalFilteredRecipients * 4) / 60); // 3 plus 1 second to send
          const config = {
            width: '450px', height: '200px', data: {
              contentAreaHeight: 120, showCancel: false, okText: 'Close', title: 'Information',
              message: (sendTestEmail)
                ? 'Email was sent to your email address.'
                : `Email sending was initiated successfully.  It will take a approximately ${timeToSend} minutes to send ${this.totalFilteredRecipients} emails.`
            }
          };
          const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
          dialogRef.afterClosed().subscribe(result => {
            if (result === 'ok' && !sendTestEmail) {
              this.back();
            }
          });
        },
        error: (error: any) => {
          const errorMessage = error.error?.message ?? error.message;
          this.errorMessagePopupService.showError(errorMessage, null, null, 'Error sending email campaign');
        },
        complete: () => {

        }
      });

  }
}
