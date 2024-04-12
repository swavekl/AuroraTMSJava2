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

  @Input()
  eventName: string;

  @Input()
  tournamentDay: number;

  @Input()
  playerName: string = null;

  @Output()
  saved: EventEmitter<PlayerStatus> = new EventEmitter<PlayerStatus>();

  @Output()
  canceled: EventEmitter<any> = new EventEmitter<any>();

  public reasonOptions: any [] = [];

  public etaControlDisabled = true;
  public reasonControlDisabled = true;

  constructor() {
    this.reasonOptions = [];
    this.reasonOptions.push({value: 'I can\'t get to the venue'});
    this.reasonOptions.push({value: 'My flight was canceled'});
    this.reasonOptions.push({value: 'I got injured'});
    this.reasonOptions.push({value: 'I am sick'});
    this.reasonOptions.push({value: 'My plans changed'});
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
        this.reasonControlDisabled = playerStatus.eventStatusCode !== 'WILL_NOT_PLAY';
      }
    }
  }

  onSave(formValues: any) {
    const updatedPlayerStatus = {
      ...this.playerStatus,
      eventStatusCode: formValues.eventStatusCode,
      estimatedArrivalTime: formValues.estimatedArrivalTime,
      reason: formValues.reason
    };
    this.saved.emit(updatedPlayerStatus);
  }

  onStatusChange($event: any) {
    if ($event.value) {
      this.etaControlDisabled = ($event.value !== 'WILL_PLAY_BUT_IS_LATE');
      this.reasonControlDisabled = ($event.value !== 'WILL_NOT_PLAY');
      const estimatedArrivalTime = this.etaControlDisabled ? '' : this.playerStatus?.estimatedArrivalTime;
      const reason = this.reasonControlDisabled ? '' : this.playerStatus?.reason;
      this.playerStatus = {
        ...this.playerStatus,
        estimatedArrivalTime: estimatedArrivalTime,
        reason: reason
      };
    }
  }
}
