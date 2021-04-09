import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';
import {RecordSearchData, UsattRecordSearchPopupComponent} from '../usatt-record-search-popup/usatt-record-search-popup.component';

/**
 * Service for showing the popup for finding players in a consistent way
 */
@Injectable({
  providedIn: 'root'
})
export class UsattRecordSearchPopupService {

  constructor(private dialog: MatDialog) { }

  showPopup(popupData: RecordSearchData, callbackData: UsattRecordSearchCallbackData) {
    const config = {
      width: '400px', height: '550px', data: popupData
    };
    const callbackScope = callbackData.callbackScope;
    const dialogRef = this.dialog.open(UsattRecordSearchPopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'ok') {
        if (callbackData.successCallbackFn != null) {
          callbackData.successCallbackFn(callbackScope, result.selectedRecord);
        }
      } else {
        if (callbackData.cancelCallbackFn != null) {
          callbackData.cancelCallbackFn(callbackScope);
        }
      }
    });
  }
}

export class UsattRecordSearchCallbackData {
  // success and failure callbacks
  successCallbackFn: (scope: any, selectedPlayerData: UsattPlayerRecord) => void;
  cancelCallbackFn: (scope: any) => void;

  // object who has the callback functions
  callbackScope: any;
}
