import {Injectable} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {ErrorMessagePopupComponent, ErrorMessagePopupData} from './error-message-popup.component';

@Injectable({
  providedIn: 'root'
})
export class ErrorMessagePopupService {

  constructor(private dialog: MatDialog) { }

  showError(errorMessage: string, popupWidth?: string, popupHeight?: string, title?: string) {
    let errorMessagePopupData: ErrorMessagePopupData = {
      errorMessage : errorMessage,
      title: title
    }

    const config: MatDialogConfig = {
      width: (popupWidth == null) ? '450px' : popupWidth,
      height: (popupHeight == null) ? '250px' : popupHeight,
      data: errorMessagePopupData
    };

    const dialogRef = this.dialog.open(ErrorMessagePopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
    });
   }
}
