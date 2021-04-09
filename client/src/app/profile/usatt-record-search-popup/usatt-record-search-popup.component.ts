import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';

/**
 * Popup for searching USATT player records with name, dob and rating
 */
@Component({
  templateUrl: './usatt-record-search-popup.component.html',
  styleUrls: ['./usatt-record-search-popup.component.css']
})
export class UsattRecordSearchPopupComponent implements OnInit {

  firstName: string;
  lastName: string;
  searchingByMembershipId: boolean;

  constructor(public dialogRef: MatDialogRef<UsattRecordSearchPopupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: RecordSearchData) {

    this.searchingByMembershipId = data.searchingByMembershipId;
    this.firstName = data.firstName;
    this.lastName = data.lastName;
  }

  ngOnInit(): void {
  }

  onSelectedPlayer(selectedRecord: UsattPlayerRecord) {
    this.dialogRef.close({action: 'ok', selectedRecord: selectedRecord});
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel'});
  }
}

export interface RecordSearchData {
  firstName: string;
  lastName: string;
  searchingByMembershipId: boolean;
}

