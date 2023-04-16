import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanDeactivate, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {EntryWizardContainerComponent} from './entry-wizard-container.component';

@Injectable({
  providedIn: 'root'
})
export class EntryWizardCanDeactivateGuard implements CanDeactivate<EntryWizardContainerComponent> {


  constructor(private dialog: MatDialog) {
  }

  canDeactivate(
    component: EntryWizardContainerComponent,
    currentRoute: ActivatedRouteSnapshot,
    currentState: RouterStateSnapshot,
    nextState?: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    if (component.isDirty()) {
      const config = {
        width: '350px', height: '360px', data: {
          contentAreaHeight: '240px',
          okText: 'Continue', cancelText: 'Discard',
          title: 'Entry not Finalized',
          message:
          `You must go through all steps of the wizard to confirm your choices by payment or refund.
          If no balance is due you must still confirm changes via 'Confirm Changes' button.
          Press Discard to discard the changes. Press Continue to continue modifying your entry.
          Unfinished entries will be discarded by the system`,
        }
      };
      const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
      dialogRef.afterClosed().subscribe(result => {
        console.log('after close message dialog');
        if (result === 'cancel') {
          component.discardChanges();
        }
      });
      return false;
    } else {
      return true;
    }
  }
}
