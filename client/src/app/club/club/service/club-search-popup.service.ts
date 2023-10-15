import {Injectable, OnDestroy} from '@angular/core';
import {Club} from '../model/club.model';
import {Subscription} from 'rxjs';
import {MatDialog} from '@angular/material/dialog';
import {ClubSearchDialogComponent} from '../club-search-dialog/club-search-dialog.component';

@Injectable({
  providedIn: 'root'
})
export class ClubSearchPopupService implements OnDestroy {

  private subscriptions: Subscription = new Subscription();

  constructor(private dialog: MatDialog) { }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  showPopup (clubSearchData: ClubSearchData, callbackData: ClubSearchCallbackData) {
    const config = {
      width: '400px', height: '600px', data: clubSearchData

    };
    const callbackScope = callbackData.callbackScope;
    const dialogRef = this.dialog.open(ClubSearchDialogComponent, config);
    const subscription = dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'ok') {
        if (callbackData.successCallbackFn != null) {
          callbackData.successCallbackFn(callbackScope, result.selectedClub);
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

export class ClubSearchCallbackData {
  // success and failure callbacks
  successCallbackFn: (scope: any, clubData: Club) => void;
  cancelCallbackFn: (scope: any) => void;

  // object who has the callback functions
  callbackScope: any;

}

export class ClubSearchData {
  state: string;

  countryCode: string;
}

