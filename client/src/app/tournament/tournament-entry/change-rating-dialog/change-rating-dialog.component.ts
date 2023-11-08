import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
  selector: 'app-change-rating-dialog',
  templateUrl: './change-rating-dialog.component.html',
  styleUrls: ['./change-rating-dialog.component.scss']
})
export class ChangeRatingDialogComponent {

  public eligibilityRating: number;
  public seedRating: number;

  public OK = 'ok';
  public CANCEL = 'cancel';

  constructor(public dialogRef: MatDialogRef<ChangeRatingDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    this.eligibilityRating = data.eligibilityRating;
    this.seedRating = data.seedRating;
  }

  onCancel() {
    this.dialogRef.close({action: this.CANCEL});
  }

  onSave() {
    this.dialogRef.close({
        action: this.OK,
        eligibilityRating: this.eligibilityRating,
        seedRating: this.seedRating
      }
    );
  }
}
