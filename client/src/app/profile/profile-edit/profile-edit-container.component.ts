import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Profile} from '../profile';
import {Observable, of, Subscription} from 'rxjs';
import {AuthenticationService} from '../../user/authentication.service';
import {ProfileService} from '../profile.service';
import {first, switchMap} from 'rxjs/operators';
import {ActivatedRoute, Router} from '@angular/router';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {UsattPlayerRecordService} from '../service/usatt-player-record.service';

@Component({
  selector: 'app-profile-edit-container',
  template: `
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
  profileId: string;
  playerRecord: UsattPlayerRecord;
  initializingProfile: boolean;

  private subscriptions: Subscription = new Subscription();
  private returnUrl: string;

  constructor(private authenticationService: AuthenticationService,
              private profileService: ProfileService,
              private usattPlayerRecordService: UsattPlayerRecordService,
              private router: Router,
              private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService) {
    this.profileId = this.activatedRoute.snapshot.params['profileId'] || '';
    if (!this.profileId || this.profileId === 'undefined') {
      this.profileId = authenticationService.getCurrentUserProfileId();
    }

    // true during on-boarding
    this.initializingProfile = (history?.state?.initializingProfile === true);
    // using existing member data during on-boarding
    this.playerRecord = history?.state?.playerData;

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
    const subscription = this.profileService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(subscription);

    // this is subscribed/unsubscibed by async pipe
    this.profile$ = this.profileService.getProfile(this.profileId)
      .pipe(
        switchMap((profile: Profile): Observable<Profile> => {
          // console.log('switchMap - got profile', profile);
          if (profile != null) {
            if (this.playerRecord != null) {
              // console.log('merging passed usatt player record into profile', this.playerRecord);
              this.merge(profile, this.playerRecord);
            }

            if (this.initializingProfile === true) {
              this.initializingProfile = false;
              let newPlayerRecord = this.playerRecord;
              if (newPlayerRecord == null) {
                // console.log ('creating new USATT membership id');
                newPlayerRecord = new UsattPlayerRecord();
                newPlayerRecord.firstName = profile.firstName;
                newPlayerRecord.lastName = profile.lastName;
              }
              return this.usattPlayerRecordService.linkPlayerToProfile(newPlayerRecord, profile.userId)
                .pipe(
                  first(),
                  switchMap ((playerRecord: UsattPlayerRecord) => {
                    // console.log('got new USATT id', playerRecord);
                    profile.membershipId = playerRecord.membershipId;
                    profile.membershipExpirationDate = playerRecord.membershipExpirationDate;
                    return of(profile);
                  }));
            } else {
              return of(profile);
            }
          }
          return of(profile);
        })
      );
  }

  /**
   *
   * @param profile
   */
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

  /**
   *
   * @param profile
   * @param playerRecord
   * @private
   */
  private merge(profile: Profile, playerRecord: UsattPlayerRecord): void {
    profile.gender = playerRecord.gender;
    profile.dateOfBirth = playerRecord.dateOfBirth;
    profile.zipCode = playerRecord.zip;
    profile.state = playerRecord.state;
    profile.gender = playerRecord.gender === 'M' ? 'Male' : 'Female';
  }

}
