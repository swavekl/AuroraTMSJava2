import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {PlayerStatusService} from '../service/player-status.service';
import {PlayerStatus} from '../model/player-status.model';
import {AuthenticationService} from '../../user/authentication.service';

@Component({
  selector: 'app-checkincommunicate',
  templateUrl: './checkin-communicate.component.html',
  styleUrls: ['./checkin-communicate.component.css']
})
export class CheckinCommunicateComponent implements OnInit {

  @Input()
  playerStatus: PlayerStatus;

  @Output()
  saved: EventEmitter<PlayerStatus> = new EventEmitter<PlayerStatus>();

  @Output()
  canceled: EventEmitter<any> = new EventEmitter<any>();

  constructor() {
  }

  ngOnInit(): void {
  }

  onCancel() {
    this.canceled.emit('canceled');
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
}
