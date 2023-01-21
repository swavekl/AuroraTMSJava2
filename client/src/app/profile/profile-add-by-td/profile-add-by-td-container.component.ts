import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ProfileService} from '../profile.service';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';
import {Profile} from '../profile';
import {first, switchMap} from 'rxjs/operators';
import {AuthenticationService} from '../../user/authentication.service';

@Component({
  selector: 'app-profile-add-by-tdcontainer',
  template: `
    <app-profile-add-by-td (createProfile)="onCreateProfile($event)"
    (useProfile)="onUseProfile($event)">
    </app-profile-add-by-td>
  `,
  styles: [
  ]
})
export class ProfileAddByTdContainerComponent implements OnInit {

  private tournamentId: string;

  constructor(private router: Router,
              private activatedRoute: ActivatedRoute,
              private profileService: ProfileService,
              private authenticationService: AuthenticationService) {
    this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'];
  }

  ngOnInit(): void {
  }

  onCreateProfile(usattPlayerRecord: UsattPlayerRecord) {
    // const profile: Profile = new Profile();
    // profile.firstName = usattPlayerRecord.firstName;
    // profile.lastName = usattPlayerRecord.lastName;
    // profile.membershipId = usattPlayerRecord.membershipId;
    // // profile.userId = usattPlayerRecord.
    // this.profileService.updateProfile(profile);
    const tempPassword: string = usattPlayerRecord.firstName + '1234';
    const email = 'swaveklorenc+' + usattPlayerRecord.firstName.toLowerCase() + '@gmail.com';
    // const subscription = this.authenticationService.register(
    //   usattPlayerRecord.firstName, usattPlayerRecord.lastName, email, tempPassword, tempPassword)
    //   .pipe(
    //     switchMap((data, index) => {
    //       // update profile based on USATT record information
    //       const profileId = '1234rfwser'; // data.profileId;
    //       this.profileService.getProfile(profileId);
    //
    //       // todo
    //       this.router.navigate(['/userprofile', profileId]);
    //     }
    //     // error => {
    //     //   // console.log('error registering', error);
    //     //   // const causes = error?.error?.errorCauses || '{}';
    //     //   // this.message = 'Error was encountered during registration: ' + JSON.stringify(causes);
    //     //   // this.okMessage = '';
    //     //   // this.registrationInProgress = false;
    //     //   // this.linearProgressBarService.setLoading(false);
    //     // })
    //   );
    // this.subscription.add(subscription);


  }

  onUseProfile(profileId: string) {

  }
}
