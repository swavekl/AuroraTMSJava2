import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Club} from '../model/club.model';
import {StatesList} from '../../../shared/states/states-list';

@Component({
  selector: 'app-club-edit',
  templateUrl: './club-edit.component.html',
  styleUrls: ['./club-edit.component.scss']
})
export class ClubEditComponent implements OnInit {

  // list of US states
  statesList: any [];
  private readonly USA_ZIP_CODE_PATTERN = /^\d{5}(?:[-\s]\d{4})?$/;
  private readonly CAN_ZIP_CODE_PATTERN = /^[ABCEGHJ-NPRSTVXY][0-9][ABCEGHJ-NPRSTV-Z] [0-9][ABCEGHJ-NPRSTV-Z][0-9]$/;
  public zipCodePattern;
  public zipCodeLength: number;


  constructor(public dialogRef: MatDialogRef<ClubEditComponent>,
              @Inject(MAT_DIALOG_DATA) public club: Club) {
    this.statesList = StatesList.getCountryStatesList('US');
  }

  ngOnInit(): void {
  }

  onSave() {
    this.dialogRef.close({action: 'ok', club: this.club});
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel'});
  }
}
