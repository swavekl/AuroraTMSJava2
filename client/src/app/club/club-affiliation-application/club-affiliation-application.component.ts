import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ClubAffiliationApplication} from '../model/club-affiliation-application.model';
import {StatesList} from '../../shared/states/states-list';
import {PlayingSite} from '../model/playing-site';
import {Router} from '@angular/router';
import {ClubAffiliationApplicationStatus} from '../model/club-affiliation-application-status';
import {UserRoles} from '../../user/user-roles.enum';
import {AuthenticationService} from '../../user/authentication.service';

@Component({
  selector: 'app-club-affiliation-application',
  templateUrl: './club-affiliation-application.component.html',
  styleUrls: ['./club-affiliation-application.component.css']
})
export class ClubAffiliationApplicationComponent implements OnInit {

  @Input()
  public clubAffiliationApplication: ClubAffiliationApplication;

  @Output()
  public saved: EventEmitter<ApplicationAndPayment> = new EventEmitter<ApplicationAndPayment>();

  statesList: any[] = [];

  constructor(private router: Router,
              private authenticationService: AuthenticationService) {
    this.statesList = StatesList.getCountryStatesList('US');
  }

  ngOnInit(): void {
  }

  onSave(payFee: boolean) {
    console.log('saving', this.clubAffiliationApplication);
    const applicationAndPayment: ApplicationAndPayment = {
      clubAffiliationApplication: this.clubAffiliationApplication,
      payFee: payFee
    };
    this.saved.emit(applicationAndPayment);
  }

  onSubmitApplication() {
    const applicationToEdit: ClubAffiliationApplication =  JSON.parse(JSON.stringify(this.clubAffiliationApplication));
    applicationToEdit.status = ClubAffiliationApplicationStatus.Submitted;
    this.clubAffiliationApplication = applicationToEdit;
    this.onSave(false);
  }

  onCancel() {
    this.router.navigateByUrl('/club/affiliationlist');
  }

  isSubmitEnabled() {
    return this.clubAffiliationApplication.status === ClubAffiliationApplicationStatus.New ||
           this.clubAffiliationApplication.status === ClubAffiliationApplicationStatus.Rejected;
  }

  isPaymentEnabled(): boolean {
    return this.clubAffiliationApplication.status === ClubAffiliationApplicationStatus.Approved;
  }

  onAddPlayingSite() {
    const clone: ClubAffiliationApplication = JSON.parse(JSON.stringify(this.clubAffiliationApplication));
    const alternatePlayingSites: PlayingSite [] = clone.alternatePlayingSites || [];
    alternatePlayingSites.push(new PlayingSite());
    clone.alternatePlayingSites = alternatePlayingSites;
    this.clubAffiliationApplication = clone;
  }

  onRemovePlayingSite(playingSiteIndex: number) {
    const clone: ClubAffiliationApplication = JSON.parse(JSON.stringify(this.clubAffiliationApplication));
    const alternatePlayingSites: PlayingSite [] = clone.alternatePlayingSites || [];
    alternatePlayingSites.splice(playingSiteIndex, 1);
    clone.alternatePlayingSites = alternatePlayingSites;
    this.clubAffiliationApplication = clone;
  }

  canSetExpirationDate() {
    const statusOK = this.clubAffiliationApplication.status === ClubAffiliationApplicationStatus.Approved ||
      this.clubAffiliationApplication.status === ClubAffiliationApplicationStatus.Completed;
    const isPermitted = this.authenticationService.hasCurrentUserRole(
      [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_OFFICIALS]);
    return isPermitted && statusOK;
  }

}

export interface ApplicationAndPayment {
  clubAffiliationApplication: ClubAffiliationApplication;
  payFee: boolean;
}
