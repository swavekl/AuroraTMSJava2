import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ProfileService} from '../profile.service';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';
import {Profile} from '../profile';
import {first, map, switchMap} from 'rxjs/operators';
import {AuthenticationService} from '../../user/authentication.service';
import {Subscription} from 'rxjs';
import {ErrorMessagePopupService} from '../../shared/error-message-dialog/error-message-popup.service';

@Component({
  selector: 'app-profile-add-by-tdcontainer',
  template: `
    <app-profile-add-by-td (createProfile)="onCreateProfile($event)"
    (useProfile)="onUseProfile($event)"
    (cancel)="onCancel($event)">
    </app-profile-add-by-td>
  `,
  styles: [
  ]
})
export class ProfileAddByTdContainerComponent implements OnInit, OnDestroy {

  private tournamentId: string;
  private returnUrl: string;

  private subscriptions: Subscription = new Subscription();

  constructor(private router: Router,
              private activatedRoute: ActivatedRoute,
              private profileService: ProfileService,
              private errorMessagePopupService: ErrorMessagePopupService) {
    this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'];
    this.returnUrl = history?.state?.returnUrl;
  }

  ngOnInit(): void {
  }

  onCancel($event: any) {
    this.router.navigateByUrl(this.returnUrl);
  }

  onCreateProfile(profile: Profile) {
    const subscription = this.profileService.createProfile(profile)
      .pipe(first())
      .subscribe((createdProfile: Profile) => {
        const url = `/ui/userprofile/edit/${createdProfile.userId}`;
        const extras = {
          state: {
            addingProfile: true,
            returnUrl: `/ui/officials/create/${createdProfile.userId}`
          }
        };
        this.router.navigateByUrl(url, extras);
      },
      (error: any) => {
        const message = `Error creating profile\n Error ${error.error}`;
        this.errorMessagePopupService.showError(message, "400px", "250px");
      });
    this.subscriptions.add(subscription);
  }

  onUseProfile(profileId: string) {
    const url = `/ui/officials/create/${profileId}`
    this.router.navigateByUrl(url);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
