import {Component, Inject, OnInit} from '@angular/core';
import {PrizeInfo} from '../../model/prize-info.model';
import {DrawMethod} from '../../model/draw-method.enum';
import {CommonRegexPatterns} from '../../../../shared/common-regex-patterns';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
    selector: 'app-prize-info-dialog',
    templateUrl: './prize-info-dialog.component.html',
    styleUrls: ['./prize-info-dialog.component.scss'],
    standalone: false
})
export class PrizeInfoDialogComponent implements OnInit {
  prizeInfo: PrizeInfo;
  drawMethod: DrawMethod;

  readonly PRICE_REGEX = CommonRegexPatterns.FIVE_DIGIT_NUMERIC_REGEX;

  readonly NUMERIC_REGEX = CommonRegexPatterns.NUMERIC_REGEX;

  constructor(public dialogRef: MatDialogRef<PrizeInfoDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: PrizeInfoDialogData) {
    this.prizeInfo = data.prizeInfo;
    this.drawMethod = data.drawMethod;
  }

  ngOnInit(): void {
  }

  onSave() {
    // set some sensible defaults
    const updatedPrizeInfo: PrizeInfo = {
      ...this.prizeInfo,
      awardedForPlace: Number(this.prizeInfo.awardedForPlace),
      awardedForPlaceRangeEnd: this.prizeInfo.awardedForPlaceRangeEnd ?? 0,
      awardTrophy: this.prizeInfo.awardTrophy ?? false
    };
    const retValue = {
      action: 'ok',
      prizeInfo: updatedPrizeInfo
    };
    this.dialogRef.close(retValue);
  }

  onCancel() {
    this.dialogRef.close({action: 'Cancel'});
  }
}

export class PrizeInfoDialogData {
  public prizeInfo: PrizeInfo;
  public drawMethod: DrawMethod;
}
