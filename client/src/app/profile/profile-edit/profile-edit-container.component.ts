import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Profile} from '../profile';
import {Observable, Subscription} from 'rxjs';
import {AuthenticationService} from '../../user/authentication.service';
import {ProfileService} from '../profile.service';
import {first, map} from 'rxjs/operators';
import {ActivatedRoute, Router} from '@angular/router';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';

@Component({
  selector: 'app-profile-edit-container',
  template: `
    <app-linear-progress-bar [loading]="loading$ | async"></app-linear-progress-bar>
    <app-profile-edit [profile]="profile$ | async"
                      (saved)="onSave($event)"
                      (canceled)="onCancel($event)">
    </app-profile-edit>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfileEditContainerComponent implements OnInit, OnDestroy {
  // this is what we edit
  profile$: Observable<Profile>;
  loading$: Observable<boolean>;
  profileId: string;
  playerRecord: UsattPlayerRecord;

  private subscriptions: Subscription = new Subscription();
  private returnUrl: string;

  constructor(private authenticationService: AuthenticationService,
              private profileService: ProfileService,
              private router: Router,
              private activatedRoute: ActivatedRoute) {
    this.profileId = this.activatedRoute.snapshot.params['profileId'] || '';
    if (!this.profileId || this.profileId === 'undefined') {
      this.profileId = authenticationService.getCurrentUserProfileId();
    }

    this.playerRecord = history?.state?.data;
    // if we are coming from registration filling in the first time
    // console.log('player data', this.playerRecord);
    // this.returnUrl = history?.state?.url;  // for when TD edits profiles
    if (this.playerRecord != null) {
      this.returnUrl = '/tournaments';
    } else {
      this.returnUrl = '/home';
    }
  }

  ngOnInit() {
    this.loading$ = this.profileService.loading$;
    // this is subscribed/unsubscibed by async pipe
    this.profile$ = this.profileService.getProfile(this.profileId)
      .pipe(
        map((profile: Profile): Profile => {
          // console.log('map - got profile', profile);
          if (profile != null) {
            if (this.playerRecord != null) {
              // console.log('merging usatt data into profile', this.playerRecord);
              this.merge(profile, this.playerRecord);
            }
          }
          return profile;
        })
      );
  }

  onSave(profile: Profile) {
    profile.userId = this.profileId;
    // update and and unsubscribe immediately with first but in case user navigates still remember subscription
    const subscription = this.profileService.updateProfile(profile)
      .pipe(first())
      .subscribe(
        () => {
          // console.log('Updated profile successfully');
          this.onCancel(null);
        },
        (err: any) => console.error(err)
      );
    this.subscriptions.add(subscription);
  }

  onCancel($event: any) {
    this.router.navigateByUrl(this.returnUrl);
  }

  ngOnDestroy(): void {
    // in case user navigates before update finishes
    this.subscriptions.unsubscribe();
  }

  private merge(profile: Profile, playerRecord: UsattPlayerRecord): void {
    profile.gender = playerRecord.gender;
    profile.dateOfBirth = playerRecord.dateOfBirth;
    profile.zipCode = playerRecord.zip;
    profile.state = playerRecord.state;
    profile.gender = playerRecord.gender === 'M' ? 'Male' : 'Female';
  }
}
