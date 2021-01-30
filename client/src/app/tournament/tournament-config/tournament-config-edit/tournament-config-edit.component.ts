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
  tournamentTypes: any [];

  constructor() {
    this.statesList = StatesList.getList();
    this.tournamentTypes = [];
    this.tournamentTypes.push({name: 'Ratings Restricted', value: 'RatingsRestricted'});
    this.tournamentTypes.push({name: 'Round Robin', value: 'RoundRobin'});
    this.tournamentTypes.push({name: 'Teams', value: 'Teams'});
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

  public get tournamentType(): string {
    return this.tournament?.configuration?.tournamentType;
  }

  public set tournamentType(tournamentType: string) {
    this.tournament.configuration.tournamentType = tournamentType;
  }

}
