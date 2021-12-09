import {Component, ElementRef, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges, ViewChild} from '@angular/core';
import {SanctionCategory, SanctionRequest, SanctionRequestStatus} from '../model/sanction-request.model';
import {MatDialog} from '@angular/material/dialog';
import {StatesList} from '../../shared/states/states-list';
import {CoordinatorInfo, coordinatorList} from '../../shared/coordinator-info';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';

@Component({
  selector: 'app-sanction-request-edit',
  templateUrl: './sanction-request-edit.component.html',
  styleUrls: ['./sanction-request-edit.component.scss']
})
export class SanctionRequestEditComponent implements OnInit, OnChanges {
  @ViewChild('ESD') ESD: ElementRef;

  // this is what we edit
  @Input() sanctionRequest: SanctionRequest;

  // save and cancel
  @Output() saved = new EventEmitter();
  @Output() canceled = new EventEmitter();

  statesList: any [];

  // current category index (lights, flooring etc)
  currentCategory: number;

  // total rating points
  totalPoints: number;

  // updated in case it changes
  venueState: string;

  minStartDate = new Date();
  maxStartDate = new Date();
  minAltStartDate = new Date();
  maxAltStartDate = new Date();

  minEndDate: Date;
  maxEndDate: Date;
  minAltEndDate: Date;
  maxAltEndDate: Date;

  endDateEnabled = false;
  altEndDateEnabled = false;

  constructor(private messageDialog: MatDialog) {
    this.currentCategory = 0;
    this.totalPoints = 0;
    this.statesList = StatesList.getList();
    this.minStartDate.setDate(this.minStartDate.getDate() + 30);
    this.maxStartDate.setDate(this.maxStartDate.getDate() + 365);
    this.minAltStartDate.setDate(this.minStartDate.getDate() + 30);
    this.maxAltStartDate.setDate(this.maxAltStartDate.getDate() + 365);
  }

  ngOnInit() {
  }

  // called after Input changes
  ngOnChanges (simpleChanges: SimpleChanges) {
    if (simpleChanges.sanctionRequest != null) {
      const sanctionRequestSimpleChange: SimpleChange = simpleChanges.sanctionRequest;
      const sanctionRequestToEdit: SanctionRequest = sanctionRequestSimpleChange.currentValue;
      if (sanctionRequestToEdit != null) {
        // convert configurationJSON from JSON string into object
        const sanctionRequest: SanctionRequest = new SanctionRequest();
        sanctionRequest.clone(sanctionRequestToEdit);
        this.sanctionRequest = sanctionRequest;

        this.totalPoints = this.calculateTotal();
      }
    }
  }

  onEnableEndDate(date: Date) {
    this.endDateEnabled = true;
    this.minEndDate = new Date(this.sanctionRequest.startDate.getTime());
    this.minEndDate.setDate(this.minEndDate.getDate() + 1);
    this.maxEndDate = new Date(this.sanctionRequest.startDate.getTime());
    this.maxEndDate.setDate(this.maxEndDate.getDate() + 7);
  }

  onEnableAltEndDate(date: Date) {
    this.altEndDateEnabled = true;
    this.minAltEndDate = new Date(this.sanctionRequest.requestContents.alternateStartDate.getTime());
    this.minAltEndDate.setDate(this.minAltEndDate.getDate() + 1);
    this.maxAltEndDate = new Date(this.sanctionRequest.requestContents.alternateStartDate.getTime());
    this.maxAltEndDate.setDate(this.maxAltEndDate.getDate() + 7);
  }

  setCategory(index: number) {
    this.currentCategory = index;
  }


  nextCategory() {
    this.currentCategory++;
  }

  prevCategory() {
    this.currentCategory--;
  }

  isCurrentCategory(index: number) {
    return this.currentCategory === index;
  }

  notFirstCategory(index: number) {
    return this.currentCategory > 0;
  }

  notLastCategory(index: number) {
    const totalCategories = this.sanctionRequest.requestContents.categories.length;
    return (index !== (totalCategories - 1));
  }

  hasNextStep () {
    return this.isApprovingCoordinator();
  }

  /**
   * transfers values from form object to SanctionRequest object
   */
  makeSanctionRequest (formValues: any): SanctionRequest {
    // copy changed values into this new object
    const sanctionRequestToSave: SanctionRequest = new SanctionRequest();
    sanctionRequestToSave.applyChanges (formValues);
    return sanctionRequestToSave;
  }

  /**
   * Save the sanction request
   */
  save(formValues: any) {
//    console.log ('formValues ', formValues);
    const sanctionRequestToSave: SanctionRequest = this.makeSanctionRequest (formValues);
//    console.log("Saving sanction request....", sanctionRequestToSave);
    this.saved.emit (sanctionRequestToSave);
  }

  onCancel () {
    this.canceled.emit('cancelled');
  }

  // save and submit for sanction
  onSaveAndSubmit (formValues: any) {
    const sanctionRequestToSave: SanctionRequest = this.makeSanctionRequest (formValues);

    // find coordinator who will receive this request and set it in the request.
    // translate long name to short state name
    const longStateName = this.translateStateName(formValues.venueState);
    const starLevel = sanctionRequestToSave.starLevel;
    const coordinatorInfo: CoordinatorInfo = this.findCoordinator(longStateName, starLevel);

    // notify user about who will be getting this request
    let message = '';
    if (coordinatorInfo != null) {
      sanctionRequestToSave.starLevel = starLevel;
      sanctionRequestToSave.coordinatorFirstName = coordinatorInfo.firstName;
      sanctionRequestToSave.coordinatorLastName = coordinatorInfo.lastName;
      sanctionRequestToSave.coordinatorEmail = coordinatorInfo.email;
      message += 'Your request has been submitted to ';
      message += coordinatorInfo.firstName + ' ' + coordinatorInfo.lastName;
      message += ' who is the ' + coordinatorInfo.region + ' region Sanction Coordinator.';
      message += ' You may follow up with him by phone ' + coordinatorInfo.phone;
      message += ' or email ' + coordinatorInfo.email;
    }

    // mark it a submitted
    sanctionRequestToSave.status = SanctionRequestStatus.Submitted;

    this.saved.emit (sanctionRequestToSave);

    // show who will get it
    this.openDialog (message);
  }

  /**
   * Translate IL to Illinois
   */
  translateStateName (stateAbbreviation: string): string {
    const stateList: any [] = StatesList.getList();
    for (let i = 0; i <  stateList.length; i++) {
      const stateObj = stateList[i];
      if (stateObj.abbreviation === stateAbbreviation) {
        return stateObj.name;
      }
    }
    return null;
  }

  /**
   * Finds regional or national coordinator
   */
  findCoordinator (state: string, starLevel: number): CoordinatorInfo {
    let coordinatorInfo: CoordinatorInfo = null;
    if (starLevel >= 4) {
      // national coordinator
      for (let i = 0; i < coordinatorList.length; i++) {
        if (coordinatorList[i].region === 'National') {
          coordinatorInfo = coordinatorList[i];
          break;
        }
      }
    } else {
      // regional coordinator
      for (let i = 0; i < coordinatorList.length; i++) {
        const cinfo: CoordinatorInfo = coordinatorList[i];
        let found = false;
        const states = cinfo.states;
        for (let k = 0; k < states.length; k++) {
          if (states[k] === state) {
            found = true;
            break;
          }
        }

        if (found) {
          coordinatorInfo = cinfo;
          break;
        }
      }
    }

    return coordinatorInfo;
  }

  openDialog(message: string): void {
    const dialogRef = this.messageDialog.open(ConfirmationPopupComponent, {
      width: '450px',
      data: { message: message, title: 'Request Submitted'} // , showCancelButton: true }
    });

    dialogRef.afterClosed().subscribe(result => {
//        console.log('The dialog was closed with result ', result);
    });
  }

  isApprovingCoordinator () {
    // check if the current user is a sanction coordinator
    // if not don't show the approve/reject step
    return false;
  }

  // event handler for when radio button is clicked
  onRadioGroupChange(event) {
    this.totalPoints = this.calculateTotal();
  }

  // event handler for when checkbox button is clicked
  onCheckBoxChange (event) {
    this.totalPoints = this.calculateTotal();
  }

  // recalculates total rating points when selection changes
  calculateTotal () {
    let total = 0;
    if (this.sanctionRequest) {
      const categories = this.sanctionRequest.requestContents.categories;
      for (let i = 0; i < categories.length; i++) {
        const category: SanctionCategory = categories[i];
        total += category.getSubTotal();
      }
    }
    return total;
  }

  // calculates which star level user qualifies for based on the rating points total
  getQualifiedStarLevel () {
    let qualifiedStarLevel = 0;
    if (this.totalPoints <= 10) {
      qualifiedStarLevel = 0;
    } else if (this.totalPoints > 10 && this.totalPoints <= 20) {
      qualifiedStarLevel = 1;
    } else if (this.totalPoints > 20 && this.totalPoints <= 30) {
      qualifiedStarLevel = 2;
    } else if (this.totalPoints > 30 && this.totalPoints <= 40) {
      qualifiedStarLevel = 3;
    } else if (this.totalPoints > 40 && this.totalPoints <= 50) {
      qualifiedStarLevel = 4;
    } else {
      qualifiedStarLevel = 5;
    }
    return qualifiedStarLevel;
  }

  onVenueStateChange (event) {
    //   console.log ('in onVenueStateChange ', event);
    this.venueState = event.value;
  }

  starLevelChanged(event) {
    // console.log ('in starLevelChanged ', event);
    const starLevel = event.srcElement.value;
    const stateName = this.venueState;
//      console.log ('starLevel ', starLevel);
    //     console.log ('stateName ', stateName);
    // find coordinator who will receive this request and set it in the request.
    const longStateName = this.translateStateName(stateName);
    const coordinatorInfo: CoordinatorInfo = this.findCoordinator(longStateName, starLevel);
//      console.log ('coordinatorInfo ', coordinatorInfo);
  }

  isApproveRejectEnabled() {
    return true;
  }
}
