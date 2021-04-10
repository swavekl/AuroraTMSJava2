import {Injectable, OnDestroy} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';
import {RecordSearchData, UsattRecordSearchPopupComponent} from '../usatt-record-search-popup/usatt-record-search-popup.component';
import {Subscription} from 'rxjs';

/**
 * Service for showing the popup for finding players in a consistent way
 */
@Injectable({
  providedIn: 'root'
})
export class UsattRecordSearchPopupService implements OnDestroy {

  private subscriptions: Subscription = new Subscription();

  constructor(private dialog: MatDialog) { }

  showPopup(popupData: RecordSearchData, callbackData: UsattRecordSearchCallbackData) {
    const config = {
      width: '400px', height: '550px', data: popupData
    };
    const callbackScope = callbackData.callbackScope;
    const dialogRef = this.dialog.open(UsattRecordSearchPopupComponent, config);
    const subscription = dialogRef.afterClosed().subscribe(result => {
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
    this.subscriptions.add(subscription);
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }
}

export class UsattRecordSearchCallbackData {
  // success and failure callbacks
  successCallbackFn: (scope: any, selectedPlayerData: UsattPlayerRecord) => void;
  cancelCallbackFn: (scope: any) => void;

  // object who has the callback functions
  callbackScope: any;
}
