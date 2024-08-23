import {Component, Inject} from '@angular/core';
import {CardsInfo} from '../../shared/cards-display/cards-info.model';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
  selector: 'app-cards-popup',
  templateUrl: './cards-popup.component.html',
  styleUrl: './cards-popup.component.scss'
})
export class CardsPopupComponent {
  public cardsInfo: CardsInfo;

  constructor(public dialogRef: MatDialogRef<CardsPopupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    this.cardsInfo = JSON.parse(JSON.stringify(data.cardsInfo));
  }

  onCardSelected(thisDialog: any, updatedCardsInfo: CardsInfo): void {
    thisDialog.cardsInfo = updatedCardsInfo;
  }

  onCancel() {
    this.dialogRef.close(null);
  }

  onIssue() {
    this.dialogRef.close(this.cardsInfo);
  }
}
