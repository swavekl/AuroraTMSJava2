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
  private forwardUrl: string;

  private subscriptions: Subscription = new Subscription();

  constructor(private router: Router,
              private activatedRoute: ActivatedRoute,
              private profileService: ProfileService,
              private errorMessagePopupService: ErrorMessagePopupService) {
    this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'];
    this.returnUrl = history?.state?.returnUrl;
    this.forwardUrl = history?.state?.forwardUrl;
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
            returnUrl: `${this.forwardUrl}/${createdProfile.userId}`
          }
        };
        this.router.navigateByUrl(url, extras);
      },
      (error: any) => {
        const errorMsg: string = (error.error != null) ? error.error : error.message;
        console.error('error creating profile', errorMsg);
        const message = `Error creating profile\n Error ${errorMsg}`;
        this.errorMessagePopupService.showError(message, "400px", "250px");
      });
    this.subscriptions.add(subscription);
  }

  onUseProfile(profileId: string) {
    const url = `${this.forwardUrl}/${profileId}`;
    this.router.navigateByUrl(url);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
