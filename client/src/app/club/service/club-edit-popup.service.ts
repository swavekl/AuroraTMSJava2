import {Injectable, OnDestroy} from '@angular/core';
import {Subscription} from 'rxjs';
import {MatDialog} from '@angular/material/dialog';
import {Club} from '../model/club.model';
import {ClubEditComponent} from '../club-edit/club-edit.component';

@Injectable({
  providedIn: 'root'
})
export class ClubEditPopupService implements OnDestroy {

  private subscriptions: Subscription = new Subscription();

  constructor(private dialog: MatDialog) { }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  showPopup(clubToEdit: Club, callbackData: ClubEditCallbackData) {
    const config = {
      width: '420px', height: '430px', data: clubToEdit
    };
    const callbackScope = callbackData.callbackScope;
    const dialogRef = this.dialog.open(ClubEditComponent, config);
    const subscription = dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'ok') {
        if (callbackData.successCallbackFn != null) {
          callbackData.successCallbackFn(callbackScope, result.club);
        }
      } else {
        if (callbackData.cancelCallbackFn != null) {
          callbackData.cancelCallbackFn(callbackScope);
        }
      }
    });
    this.subscriptions.add(subscription);
  }
}

export class ClubEditCallbackData {
  // success and failure callbacks
  successCallbackFn: (scope: any, clubData: Club) => void;
  cancelCallbackFn: (scope: any) => void;

  // object who has the callback functions
  callbackScope: any;
}

