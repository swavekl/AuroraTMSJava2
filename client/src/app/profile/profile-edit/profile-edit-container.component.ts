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
import {Regions} from '../../shared/regions';
import {UserRoles} from '../../user/user-roles.enum';

@Component({
  selector: 'app-profile-edit-container',
  template: `
    <app-profile-edit [profile]="profile$ | async"
                      [canChangeMembershipId]="canChangeMembershipId"
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
  onBoarding: boolean;
  newMember: boolean;
  canChangeMembershipId: boolean;

  private subscriptions: Subscription = new Subscription();
  private returnUrl: string;
  // membership id saved so we can detect if user changed membership id
  private savedMembershipId: number;

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
    this.onBoarding = this.initializingProfile;
    // using existing member data during on-boarding
    this.playerRecord = history?.state?.playerRecord;
    this.newMember = (this.initializingProfile && this.playerRecord == null);

    // if we are coming from registration filling in the first time
    if (this.initializingProfile) {
      this.returnUrl = '/onboardcomplete';
    } else {
      // console.log('player data', this.playerRecord);
      // this.returnUrl = history?.state?.url;  // for when TD edits profiles
      this.returnUrl = '/home';
    }
    // check if user can change membership id - sometimes it is necessary when they make a mistake
    const roles = [UserRoles.ROLE_TOURNAMENT_DIRECTORS, UserRoles.ROLE_ADMINS];
    this.canChangeMembershipId = this.authenticationService.hasCurrentUserRole(roles);
    this.savedMembershipId = 0;
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
              console.log('merging passed usatt player record into profile', this.playerRecord);
              this.merge(profile, this.playerRecord);
            }
            this.savedMembershipId = profile.membershipId;

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
                    // console.log('got new USATT id', JSON.stringify(playerRecord));
                    profile.membershipId = playerRecord.membershipId;
                    profile.membershipExpirationDate = playerRecord.membershipExpirationDate;
                    this.savedMembershipId = profile.membershipId;
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
   * Saves the profile and optionally updates the link between the player profile and usatt membership id
   * @param profile
   */
  onSave(profile: Profile) {
    profile.userId = this.profileId;
    // update and and unsubscribe immediately with first but in case user navigates still remember subscription
    const subscription = this.profileService.updateProfile(profile)
      .pipe(first())
      .subscribe(
        () => {
          // console.log ('SAVED profile');
          const membershipIdChanged = profile.membershipId !== this.savedMembershipId;
          if (!membershipIdChanged) {
            this.navigateToNextPage(profile);
          } else {
            // console.log ('updating membership id ' + this.savedMembershipId + ' new ' + profile.membershipId);
            // first save the new membership link and then navigate
            const usattPlayerRecord = new UsattPlayerRecord();
            usattPlayerRecord.firstName = profile.firstName;
            usattPlayerRecord.lastName = profile.lastName;
            usattPlayerRecord.membershipId = profile.membershipId;
            this.usattPlayerRecordService.linkPlayerToProfile(usattPlayerRecord, this.profileId)
              .pipe(first())
              .subscribe(
                () => {
                  this.navigateToNextPage(profile);
                },
                (error: any) => {
                  console.error ('got error linking player to ' + error);
                },
                () => {
                  // console.log ('completed observable for linkPlayertoProfile');
                }
              );
          }
        },
        (err: any) => console.error(err)
      );
    this.subscriptions.add(subscription);
  }

  /**
   * Navigates to the next page
   * @param profile
   */
  navigateToNextPage (profile: Profile) {
    // console.log('Updated profile successfully - navigating to next page ' + this.returnUrl);
    if (this.onBoarding) {
      // for US players figure out the region which they are part of
      const memberRegion: string = this.findNewMembersRegion(profile);
      const extras = {state: {
          newMember: this.newMember,
          membershipId: profile.membershipId,
          firstName: profile.firstName,
          memberRegion: memberRegion
        }};
      this.router.navigateByUrl(this.returnUrl, extras);
    } else {
      this.router.navigateByUrl(this.returnUrl);
    }
  }

  /**
   * Finds member's region based on his/her profile
   * @param profile
   * @private
   */
  private findNewMembersRegion (profile: Profile): string {
    let memberRegion: string = null;
    if (profile.countryCode === 'US') {
      const regions = new Regions().getList();
      for (const region of regions) {
        const regionStates = region.states;
        for (const state of regionStates) {
          if (state === profile.state) {
            memberRegion = region.name;
            break;
          }
        }
      }
    }
    return memberRegion;
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
    profile.firstName = playerRecord.firstName;
    profile.lastName = playerRecord.lastName;
    profile.gender = playerRecord.gender === 'M' ? 'Male' : 'Female';
    profile.dateOfBirth = playerRecord.dateOfBirth;
    profile.zipCode = playerRecord.zip;
    profile.state = playerRecord.state;
    profile.tournamentRating = playerRecord.tournamentRating;
  }

}
