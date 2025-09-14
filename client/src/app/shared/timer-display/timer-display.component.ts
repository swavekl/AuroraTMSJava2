import {ChangeDetectorRef, Component, EventEmitter, Input, Output} from '@angular/core';
import {takeWhile, timer} from 'rxjs';
import {finalize, tap} from 'rxjs/operators';

@Component({
    selector: 'app-timer-display',
    template: `
    <div class="timer-display" *ngIf="timerRunning">{{ label }} {{ timerValue | timerFormatter }}</div>
  `,
    styles: `
    div.timer-display {
        font-size: 60px;
    }
  `,
    standalone: false
})
export class TimerDisplayComponent {
  timerValue: number = 0;
  timerRunning: boolean = false;

  @Input()
  label: string = '';

  @Output()
  timerExpired: EventEmitter<any> = new EventEmitter();

  constructor(private cdr: ChangeDetectorRef) {
  }

  startTimer(duration: number) {
    if (!this.timerRunning) {
      this.timerRunning = true;
      this.timerValue = duration;
      this.cdr.detectChanges();
      timer(1000, 1000)
        .pipe(
          takeWhile( () => this.timerValue > 0 ),
          tap(() => this.timerValue--),
          finalize(() => {
            this.timerRunning = false;
            this.timerValue = 0;
            this.timerExpired.emit(`${this.label} expired`);
          })
        ).subscribe( );
    }
  }

  stopTimer() {
    if (this.timerRunning) {
      this.timerValue = 0;
    }
  }

  isTimerRunning() {
    return this.timerRunning;
  }
}
