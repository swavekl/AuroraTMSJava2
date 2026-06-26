import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from '../../user/authentication.service';
import { UsattPlayerRecordService } from '../service/usatt-player-record.service';
import { UsattPlayerRecord } from '../model/usatt-player-record.model';
import { first } from 'rxjs/operators';
import { Router } from '@angular/router';
import { UsattRecordSearchCallbackData, UsattRecordSearchPopupService } from '../service/usatt-record-search-popup.service';
import { RecordSearchData } from '../usatt-record-search-popup/usatt-record-search-popup.component';

@Component({
  selector: 'app-profile-edit-start',
  templateUrl: './profile-edit-start.component.html',
  styleUrls: ['./profile-edit-start.component.scss'],
  standalone: false
})
export class ProfileEditStartComponent implements OnInit {
  firstName: string;
  lastName: string;
  playerRecordFound: boolean;
  playerRecord: UsattPlayerRecord;
  profileId: string;

  // Track unique constraint collision states reactively
  isUniqueConstraintViolation: boolean = false;
  maskedEmail: string = null;

  constructor(private authenticationService: AuthenticationService,
              private usattPlayerRecordService: UsattPlayerRecordService,
              private playerFindPopupService: UsattRecordSearchPopupService,
              private router: Router) {
    this.firstName = this.authenticationService.getCurrentUserFirstName();
    this.lastName = this.authenticationService.getCurrentUserLastName();
    this.profileId = this.authenticationService.getCurrentUserProfileId();
    this.playerRecordFound = false;
  }

  ngOnInit(): void {
    this.usattPlayerRecordService.getByNames(this.firstName, this.lastName)
      .pipe(first())
      .subscribe((record: UsattPlayerRecord) => {
        if (record != null) {
          this.playerRecordFound = true;
          this.playerRecord = record;
          // Run check on the initial auto-matched record
          this.checkRecordAvailability(record);
        } else {
          this.playerRecordFound = false;
        }
      });
  }

  /**
   * Centralized mapping gatekeeper. Checks availability and toggles UI flags.
   */
  private checkRecordAvailability(record: UsattPlayerRecord): void {
    if (!record || !record.membershipId) return;

    this.usattPlayerRecordService.checkMembershipMappingAvailability(record.membershipId, this.profileId)
      .pipe(first())
      .subscribe((result: any) => {
        this.isUniqueConstraintViolation = result.isAvailable != 'true';
        this.maskedEmail = result.maskedEmail;
      });
  }

  /**
   * Resets the application state when the user chooses to escape the collision warning
   */
  resetCollisionState(): void {
    this.isUniqueConstraintViolation = false;
    this.maskedEmail = null;
    this.clearUsattRecord(); // This sets playerRecordFound = false and clears playerRecord
  }

  /**
   * Click Handler: Triggers when the user selects 'Fill Profile'
   */
  onVerifyAndStartEdit(initializingProfile: boolean, playerRecord: UsattPlayerRecord) {
    if (this.isUniqueConstraintViolation) return; // Fail-fast safety block

    // Proceed to edit screen if it has cleared check rules
    this.onProfileEditStart(initializingProfile, playerRecord);
  }

  onProfileEditStart(initializingProfile: boolean, playerRecord: UsattPlayerRecord) {
    const state = {initializingProfile: initializingProfile, playerRecord: playerRecord};
    const url = `/ui/userprofile/edit/${this.profileId}`;
    this.router.navigate([url], {state: state});
  }

  onFindPlayerById() {
    this.findPlayer(true);
  }

  onFindPlayerByName() {
    this.findPlayer(false);
  }

  private findPlayer(searchById: boolean) {
    const data: RecordSearchData = {
      firstName: null,
      lastName: this.lastName,
      searchingByMembershipId: searchById
    };
    const callbackParams: UsattRecordSearchCallbackData = {
      successCallbackFn: this.onFindPlayerOkCallback,
      cancelCallbackFn: null,
      callbackScope: this // Explicitly binding context scope
    };
    this.playerFindPopupService.showPopup(data, callbackParams);
  }

  /**
   * FIXED MANUAL SEARCH CALLBACK ROUTINE:
   * Triggers immediately when a record is chosen from the popup window.
   */
  onFindPlayerOkCallback(scope: any, selectedPlayerRecord: UsattPlayerRecord) {
    const me = scope;

    // 1. Reset error state cleanly upon a fresh selection
    me.isUniqueConstraintViolation = false;
    me.maskedEmail = null;

    // 2. Bind newly discovered dataset to the template models
    me.playerRecord = selectedPlayerRecord;
    me.firstName = selectedPlayerRecord.firstName;
    me.lastName = selectedPlayerRecord.lastName;
    me.playerRecordFound = true;

    // 3. IMMEDIATELY check if this manual selection causes an account violation
    me.checkRecordAvailability(selectedPlayerRecord);
  }

  clearUsattRecord() {
    this.playerRecordFound = false;
    this.playerRecord = null;
    this.isUniqueConstraintViolation = false;
    this.maskedEmail = null;
  }

  createNewProfile() {
    const usattPlayerRecord: UsattPlayerRecord = new UsattPlayerRecord();
    usattPlayerRecord.firstName = this.authenticationService.getCurrentUserFirstName();
    usattPlayerRecord.lastName = this.authenticationService.getCurrentUserLastName();
    usattPlayerRecord.gender = 'M';
    this.onProfileEditStart(true, usattPlayerRecord);
  }

  visitJustGo() {
    window.open('https://usatt.justgo.com/', '_blank');
  }

  /**
   * Spawns mailto hook or helpdesk dialog interaction to alert admin support logs
   */
  onContactAdmin(): void {
    const email = 'swaveklorenc@yahoo.com';
    const subject = 'TTAurora Account Recovery Request';

    // Using bullet points or pipes keeps it readable when Yahoo flattens it
    const body = `Hello Admin, ` +
      `Request: Please help me recover my account. ` +
      `Name: ${this.playerRecord?.firstName} ${this.playerRecord?.lastName} ` +
      `USATT ID: ${this.playerRecord?.membershipId} ` +
      `Thank you!`;

    window.location.href = `mailto:${email}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
  }
}
