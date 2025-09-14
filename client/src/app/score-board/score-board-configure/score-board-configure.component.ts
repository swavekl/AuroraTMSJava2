import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';

@Component({
    selector: 'app-score-board-configure',
    templateUrl: './score-board-configure.component.html',
    styleUrls: ['./score-board-configure.component.scss'],
    standalone: false
})
export class ScoreBoardConfigureComponent implements OnInit, OnChanges {

  // tournaments to which this scorebard has the ability to connect for live scoring
  @Input()
  tournaments: any[];

  @Output()
  tableSelected: EventEmitter<any> = new EventEmitter<any>();

  // tournament at which to use digital score board the table
  tournamentId: number;

  // day of the tournament
  tournamentDay: number;

  // table number at which to use the digital score board
  selectedTable: number;

  // array of tournament tables to choose from
  tournamentTables: number[] = [];

  constructor() {
  }

  ngOnInit(): void {
  }

  onChangeTournament(strTournamentId: string) {
    if (this.tournaments != null && strTournamentId !== '') {
      const tournamentId = Number(strTournamentId);
      for (let i = 0; i < this.tournaments.length; i++) {
        const tournamentInfo = this.tournaments[i];
        if (tournamentInfo.id === tournamentId) {
          this.tournamentId = tournamentInfo.id;
          this.tournamentDay = tournamentInfo.tournamentDay;
          this.tournamentTables = this.makeArrayOfTournamentTables(tournamentInfo);
        }
      }
    }
  }

  onSelectTable(tableNumber: number) {
    const result = {
      tournamentId: this.tournamentId,
      tournamentDay: this.tournamentDay,
      tableNumber: tableNumber
    };
    this.tableSelected.emit(result);
  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentsChanges: SimpleChange = changes.tournaments;
    if (tournamentsChanges != null) {
      const tournamentInfos = tournamentsChanges.currentValue;
      if (tournamentInfos != null) {
        if (tournamentInfos.length === 1) {
          const tournamentInfo = tournamentInfos[0];
          this.tournamentId = tournamentInfo.id;
          this.tournamentDay = tournamentInfo.tournamentDay;
          this.tournamentTables = this.makeArrayOfTournamentTables(tournamentInfo);
        }
      }
    }
  }

  private makeArrayOfTournamentTables(tournamentInfo: any): number [] {
    const monitoredTables: string = tournamentInfo.monitoredTables;
    const strTableNumbers: string [] = monitoredTables.split(',');
    const tournamentTables = [];
    for (let j = 0; j < strTableNumbers.length; j++) {
      const strTableNumber = strTableNumbers[j];
      tournamentTables.push(Number(strTableNumber));
    }
    return tournamentTables;
  }
}
