import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Tournament} from '../tournament.model';
import {StatesList} from '../../../shared/states/states-list';

@Component({
  selector: 'app-tournament-config-edit',
  templateUrl: './tournament-config-edit.component.html',
  styleUrls: ['./tournament-config-edit.component.css']
})
export class TournamentConfigEditComponent implements OnInit {

  @Input()
  tournament: Tournament;

  // save and cancel
  @Output() saved = new EventEmitter();
  @Output() canceled = new EventEmitter();

  // list of US states
  statesList: any [];

  constructor() {
    this.statesList = new StatesList().getList();
  }

  ngOnInit(): void {
  }

  onEnter() {
    console.log('entering the tournament');
  }

  onSave(formValues: any) {
    const tournament = Tournament.toTournament(formValues);
    this.saved.emit(tournament);
  }

  onCancel() {
    this.canceled.emit('cancelled');
  }
}
