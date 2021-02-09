import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Profile} from '../profile';
import {Observable, Subscription} from 'rxjs';
import {AuthenticationService} from '../../user/authentication.service';
import {ProfileService} from '../profile.service';
import {first} from 'rxjs/operators';
import {Router} from '@angular/router';

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

  private subscriptions: Subscription = new Subscription();

  constructor(private authenticationService: AuthenticationService,
              private profileService: ProfileService,
              private router: Router) {
  }

  ngOnInit() {
    const currentUser = this.authenticationService.getCurrentUser();
    this.profile$ = this.profileService.getProfile(currentUser.id);
    this.loading$ = this.profileService.loading$;
  }

  onSave(profile: Profile) {
    const currentUser = this.authenticationService.getCurrentUser();
    profile.userId = currentUser.id;
    // update and and unsubscribe immediately
    const subscription = this.profileService.updateProfile(profile)
      .pipe(first())
      .subscribe(
        () => {
          console.log ('Updated profile successfully');
          this.onCancel(null);
        },
        (err: any) => console.error(err)
      );
    this.subscriptions.add(subscription);
  }

  onCancel($event: any) {
    this.router.navigateByUrl('/home');
  }

  ngOnDestroy(): void {
    // in case user navigates before update finishes
    this.subscriptions.unsubscribe();
  }
}
