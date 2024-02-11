import {Component, OnDestroy} from '@angular/core';
import {Router} from '@angular/router';
import {Observable, of, Subscription} from 'rxjs';

import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';

import {Registration} from '../model/registration.model';
import {RegistrationService} from '../service/registration.service';
import {AuthenticationService} from '../../user/authentication.service';
import {first, map} from 'rxjs/operators';
import {RegistrationEventType} from '../model/registration-event-type.enum';

@Component({
  selector: 'app-registration-list-container',
  template: `
      <app-registration-list [registrations]="registrations$ | async"
                             (eventEmitter)="onView($event)">

      </app-registration-list>
  `,
  styles: []
})
export class RegistrationListContainerComponent implements OnDestroy {

  public registrations$: Observable<Registration[]>;
  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();

  constructor(private router: Router,
              private linearProgressBarService: LinearProgressBarService,
              private registrationService: RegistrationService,
              private authenticationService: AuthenticationService) {
    this.setupProgressIndicator();
    this.loadRegistrations();
  }

  private setupProgressIndicator() {
    this.loading$ = this.registrationService.loading$;

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadRegistrations() {
    let currentUserProfileId = this.authenticationService.getCurrentUserProfileId();
    let subscription = this.registrationService.load(currentUserProfileId)
      .pipe(first(),
        map((registrations: Registration[]) => {
            this.registrations$ = of(registrations);
          }
        ))
      .subscribe();
    this.subscriptions.add(subscription);

  }

  onView(registration: Registration) {
    if (registration.registrationEventType === RegistrationEventType.TOURNAMENT) {
      const url = `/ui/entries/entryview/${registration.activityId}/edit/${registration.id}`;
      const extras = {
        state: {
          returnUrl: `/ui/registrations`,
        }
      };
      this.router.navigate([url], extras);
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
