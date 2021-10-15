import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {PlayerStatus} from '../model/player-status.model';

@Component({
  selector: 'app-checkincommunicate',
  templateUrl: './checkin-communicate.component.html',
  styleUrls: ['./checkin-communicate.component.css']
})
export class CheckinCommunicateComponent implements OnInit, OnChanges {

  @Input()
  playerStatus: PlayerStatus;

  @Output()
  saved: EventEmitter<PlayerStatus> = new EventEmitter<PlayerStatus>();

  @Output()
  canceled: EventEmitter<any> = new EventEmitter<any>();

  public etaControlDisabled = true;

  constructor() {
  }

  ngOnInit(): void {
  }

  onCancel() {
    this.canceled.emit('canceled');
  }

  ngOnChanges(changes: SimpleChanges): void {
    const playerStatusChange: SimpleChange = changes.playerStatus;
    if (playerStatusChange != null) {
      const playerStatus: PlayerStatus = playerStatusChange.currentValue;
      if (playerStatus != null) {
        this.etaControlDisabled = playerStatus.eventStatusCode !== 'WILL_PLAY_BUT_IS_LATE';
      }
    }
  }

  onSave(formValues: any) {
    console.log('on Save formValues', formValues);
    const updatedPlayerStatus = {
      ...this.playerStatus,
      eventStatusCode: formValues.eventStatusCode,
      estimatedArrivalTime: formValues.estimatedArrivalTime
    };
    this.saved.emit(updatedPlayerStatus);
  }

  onStatusChange($event: any) {
    if ($event.value) {
      const disabled = ($event.value !== 'WILL_PLAY_BUT_IS_LATE');
      if (disabled) {
        this.playerStatus = {...this.playerStatus, estimatedArrivalTime: ''};
      }
      this.etaControlDisabled = disabled;
    }
  }
}
