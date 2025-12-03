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

  public otherAwardType = null;

  constructor(public dialogRef: MatDialogRef<PrizeInfoDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: PrizeInfoDialogData) {
    // console.log('data', data);
    const isOther = data.prizeInfo.awardTrophy &&
      (data.prizeInfo.awardType !== 'Trophy' && data.prizeInfo.awardType !== 'Medal');
    const isNone = !data.prizeInfo.awardTrophy;
    const awardType = isOther ? 'Other' :  (isNone ? 'None' : data.prizeInfo.awardType);
    this.prizeInfo = {
      ...data.prizeInfo,
      awardType: awardType
    };

    this.drawMethod = data.drawMethod;
    this.otherAwardType = (this.isOtherAwardFieldDisabled()) ? null : data.prizeInfo.awardType;
  }

  ngOnInit(): void {
  }

  onSave() {

    // console.log("this.otherAwardType", this.otherAwardType);
    // console.log('this.prizeInfo', this.prizeInfo);
    // set some sensible defaults
    const awardTrophy = this.prizeInfo.awardType !== "None";
    const otherAwardIsSet = this.otherAwardType != null && this.otherAwardType !== '';
    const awardType = (otherAwardIsSet) ? this.otherAwardType : (awardTrophy ? this.prizeInfo.awardType : null);
    const updatedPrizeInfo: PrizeInfo = {
      ...this.prizeInfo,
      awardedForPlace: Number(this.prizeInfo.awardedForPlace),
      awardedForPlaceRangeEnd: this.prizeInfo.awardedForPlaceRangeEnd ?? 0,
      awardTrophy: awardTrophy,
      awardType: awardType
    };
    // console.log("updatedPrizeInfo", updatedPrizeInfo);
    const retValue = {
      action: 'ok',
      prizeInfo: updatedPrizeInfo
    };
    this.dialogRef.close(retValue);
  }

  onCancel() {
    this.dialogRef.close({action: 'Cancel'});
  }

  protected isOtherAwardFieldDisabled() {
     return this.prizeInfo?.awardType === "None" || this.prizeInfo?.awardType === "Medal" || this.prizeInfo?.awardType === "Trophy";
  }
}

export class PrizeInfoDialogData {
  public prizeInfo: PrizeInfo;
  public drawMethod: DrawMethod;
}
