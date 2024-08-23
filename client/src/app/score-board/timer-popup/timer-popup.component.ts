import {AfterContentChecked, AfterContentInit, AfterViewInit, Component, Inject, ViewChild} from '@angular/core';
import {SharedModule} from '../../shared/shared.module';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';
import {TimerDisplayComponent} from '../../shared/timer-display/timer-display.component';
import {FlexModule} from 'ng-flex-layout';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-timer-popup',
  standalone: true,
  imports: [
    SharedModule,
    MatDialogContent,
    MatDialogTitle,
    MatDialogActions,
    MatButton,
    FlexModule,
    NgIf
  ],
  templateUrl: './timer-popup.component.html',
  styleUrl: './timer-popup.component.scss'
})
export class TimerPopupComponent implements AfterViewInit {

  @ViewChild(TimerDisplayComponent)
  timerDisplay: TimerDisplayComponent;

  duration: number;
  title: string;
  eventName: string;

  constructor(public dialogRef: MatDialogRef<TimerPopupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    this.duration = data.duration;
    this.title = data.title;
    this.eventName = data.eventName;
  }

  ngAfterViewInit(): void {
    if (this.timerDisplay != null) {
      this.timerDisplay.startTimer(this.duration);
    }
  }

  onStopTimer() {
    if (this.timerDisplay != null) {
      this.timerDisplay.stopTimer();
    }
    this.dialogRef.close(this.eventName);
  }
}
