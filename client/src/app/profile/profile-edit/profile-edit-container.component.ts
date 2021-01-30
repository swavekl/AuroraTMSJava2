import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Profile} from '../profile';
import {Observable} from 'rxjs';
import {AuthenticationService} from '../../user/authentication.service';
import {ProfileService} from '../profile.service';

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
export class ProfileEditContainerComponent implements OnInit {
  // this is what we edit
  profile$: Observable<Profile>;
  loading$: Observable<boolean>;

  constructor(private authenticationService: AuthenticationService,
              private profileService: ProfileService) {
  }

  ngOnInit() {
    const currentUser = this.authenticationService.getCurrentUser();
    this.profile$ = this.profileService.getProfile(currentUser.id);
    this.loading$ = this.profileService.loading$;
  }

  onSave(profile: Profile) {
    const currentUser = this.authenticationService.getCurrentUser();
    profile.userId = currentUser.id;
    this.profileService.updateProfile(profile)
      .subscribe(
        () => console.log ('Updated profile successfully'),
        (err: any) => console.error(err)
      );
  }

  onCancel($event: any) {

  }
}
