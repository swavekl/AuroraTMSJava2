import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {Tournament} from '../tournament.model';
import {StatesList} from '../../../shared/states/states-list';
import {MatTabGroup} from '@angular/material/tabs';

@Component({
  selector: 'app-tournament-config-edit',
  templateUrl: './tournament-config-edit.component.html',
  styleUrls: ['./tournament-config-edit.component.css']
})
export class TournamentConfigEditComponent {

  @Input()
  tournament: Tournament;

  // save and cancel
  @Output() saved = new EventEmitter();
  @Output() canceled = new EventEmitter();

  @ViewChild(MatTabGroup)
  tabGroup: MatTabGroup;

  // list of US states
  statesList: any [];

  constructor() {
    this.statesList = new StatesList().getList();
  }

  setActiveTab(tabIndex: number) {
    if (this.tabGroup) {
      this.tabGroup.selectedIndex = tabIndex;
    }
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
