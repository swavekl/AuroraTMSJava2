import {AfterViewInit, Component, Inject, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {TimerDisplayComponent} from '../../shared/timer-display/timer-display.component';

@Component({
    selector: 'app-timer-popup',
    templateUrl: './timer-popup.component.html',
    styleUrl: './timer-popup.component.scss',
    standalone: false
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
