import {Component, OnDestroy} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {first} from 'rxjs/operators';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {EmailService} from '../service/email.service';

@Component({
  selector: 'app-email-container',
  template: `
    <app-email (eventEmitter)="onEvent($event)"
    [tournamentName]="tournamentName"
    [emailAddresses]="emailAddresses">
    </app-email>
  `,
  styles: [
  ]
})
export class EmailContainerComponent implements OnDestroy {

  private tournamentId: number;
  tournamentName: string;
  emailAddresses: string;

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();

  constructor(private emailService: EmailService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    this.tournamentName = this.activatedRoute.snapshot.params['tournamentName'] || 'N/A';
    this.emailAddresses = '';
    this.setupProgressIndicator();
  }

  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest([
        this.emailService.loading$
      ],
      (emailsLoading: boolean) => {
        return emailsLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  onEvent($event: string) {
    if ($event === 'getemails') {
      this.emailService.getTournamentEmails(this.tournamentId)
        .pipe(first())
        .subscribe(
          (emails: string[])=> {
            const emailsCommaSeparated = emails.join(',');
            // console.log('emails', emailsCommaSeparated);
            this.emailAddresses = emailsCommaSeparated;
        });
    } else if ($event === 'back') {
      const returnUrl: string = `/ui/tournamentsconfig`;
      this.router.navigateByUrl(returnUrl);
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
