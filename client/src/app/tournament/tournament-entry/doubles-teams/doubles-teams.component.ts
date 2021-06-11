import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChange,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {DoublesPair} from '../model/doubles-pair.model';
import {TournamentEventEntry} from '../model/tournament-event-entry.model';
import {DoublesPairInfo} from '../model/doubles-pair-info.model';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {DoublesPairDialogComponent, DoublesPairingData, DoublesPairingInfo} from '../doubles-pair-dialog/doubles-pair-dialog.component';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';

@Component({
  selector: 'app-doubles-teams',
  templateUrl: './doubles-teams.component.html',
  styleUrls: ['./doubles-teams.component.scss']
})
export class DoublesTeamsComponent implements OnInit, OnChanges, AfterViewInit {

  // information about doubles events - usually a few only
  @Input()
  doublesEvents: TournamentEvent[] = [];

  @Input()
  selectedDoublesEventId: number;

  // pairs for the currently displayed event
  @Input()
  doublesPairInfos: DoublesPairInfo[] = [];

  // list of entries into the currently selected event
  @Input()
  doublesEventEntries: TournamentEventEntry[] = [];

  @Input()
  tournamentEntryInfos: TournamentEntryInfo[] = [];

  @Output()
  private selectionChangeEmitter: EventEmitter<number> = new EventEmitter<number>();

  @Output()
  private makePairEmitter: EventEmitter<DoublesPair> = new EventEmitter<DoublesPair>();

  @Output()
  private breakPairEmitter: EventEmitter<DoublesPair> = new EventEmitter<DoublesPair>();

  displayedColumns: string[] = ['playerAName', 'playerARating', 'playerBName', 'playerBRating', 'combinedRating', 'breakPair'];

  // @ViewChild(MatSort)
  // sort: MatSort;

  dataSource: MatTableDataSource<DoublesPairInfo>;

  constructor(public dialog: MatDialog) {
    this.dataSource = new MatTableDataSource(this.doublesPairInfos);
    // this.dataSource.sortingDataAccessor = (data, col) => {
    //   if (col === 'combinedRating') {
    //     return data.doublesPair.seedRating;
    //   } else {
    //     return data[col];
    //   }
    // };
  }

  ngOnInit(): void {
  }

  ngAfterViewInit() {
    // this.dataSource.sort = this.sort;
  }

  ngOnChanges(changes: SimpleChanges): void {
    // const doublesEventEntriesChanges: SimpleChange = changes.doublesEventEntries;
    // if (doublesEventEntriesChanges) {
    //   const doublesEvents = doublesEventEntriesChanges.currentValue;
    //   if (doublesEvents) {
    //     console.log('got doubles events of length ' + doublesEvents.length);
    //   } else {
    //     console.log('empty doubles events');
    //   }
    // }
    //
    const doublesPairInfosChange: SimpleChange = changes.doublesPairInfos;
    if (doublesPairInfosChange) {
      const doublesPairInfos = doublesPairInfosChange.currentValue;
      if (doublesPairInfos) {
        console.log('got doubles pair infos length ' + doublesPairInfos.length);
        this.dataSource = new MatTableDataSource(this.doublesPairInfos);
        // this.dataSource.sortingDataAccessor = (data, col) => {
        //   if (col === 'combinedRating') {
        //     return data.doublesPair.seedRating;
        //   } else {
        //     return data[col];
        //   }
        // };
        // this.dataSource.sort = this.sort;
      } else {
        console.log('emtpty doubles pair infos');
      }
    }
    //
    // const tournamentEntryInfosChange: SimpleChange = changes.tournamentEntryInfos;
    // if (tournamentEntryInfosChange) {
    //   const tournamentEntryInfos = tournamentEntryInfosChange.currentValue;
    //   if (tournamentEntryInfos) {
    //     console.log('got tournament entry infos length ' + tournamentEntryInfos.length);
    //   } else {
    //     console.log('empty tournament entry infos');
    //   }
    // }
  }

  onSelectEvent(doublesEvent: TournamentEvent) {
    this.selectedDoublesEventId = doublesEvent.id;
    this.selectionChangeEmitter.emit(doublesEvent.id);
  }

  isSelected(doublesEvent: TournamentEvent) {
    return this.selectedDoublesEventId === doublesEvent.id;
  }

  onBreakPair(doublesPair: DoublesPair) {
    this.breakPairEmitter.emit(doublesPair);
  }

  onMakePair() {
    // prepare data
    const dialogData = this.makeDialogData();
    const config: MatDialogConfig = {
      width: '330px', height: '300px', data: dialogData
    };
    // show pairing dialog
    const dialogRef = this.dialog.open(DoublesPairDialogComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'ok') {
        const doublesPair: DoublesPair = this.makeDoublesPair(result);
        this.makePairEmitter.emit(doublesPair);
      }
    });
  }

  /**
   * Finds unpaired players and makes information about them
   * @private
   */
  private makeDialogData() {
    // find unpaired players
    const pairedPlayersEntryIds: number[] = [];
    for (const doublesPairInfo of this.doublesPairInfos) {
      pairedPlayersEntryIds.push(doublesPairInfo.doublesPair.playerAEventEntryFk);
      pairedPlayersEntryIds.push(doublesPairInfo.doublesPair.playerBEventEntryFk);
    }

    // prepare information for the dialog
    const doublesPairingInfos: DoublesPairingInfo[] = [];
    for (const eventEntry of this.doublesEventEntries) {
      if (!pairedPlayersEntryIds.includes(eventEntry.id)) {
        const tournamentEntryFk = eventEntry.tournamentEntryFk;
        for (const tournamentEntryInfo of this.tournamentEntryInfos) {
          if (tournamentEntryFk === tournamentEntryInfo.entryId) {
            const playerName = tournamentEntryInfo.firstName + ' ' + tournamentEntryInfo.lastName;
            const doublesPairingInfo: DoublesPairingInfo = {
              eventEntryId: eventEntry.id,
              playerName: playerName
            };
            doublesPairingInfos.push(doublesPairingInfo);
            break;
          }
        }
      }
    }

    const dialogData: DoublesPairingData = {
      doublesPairingInfos: doublesPairingInfos
    };
    return dialogData;
  }


  /**
   * Makes a new double pair entry to confirm the pairing
   * @param result
   * @private
   */
  private makeDoublesPair(result: any): DoublesPair {
    const playerATournamentEntryInfo = this.findPlayerEntryInfo(result.playerAEventEntryId);
    const playerBTournamentEntryInfo = this.findPlayerEntryInfo(result.playerBEventEntryId);
    return {
      id: null,
      tournamentEventFk: this.selectedDoublesEventId,
      playerAEventEntryFk: result.playerAEventEntryId,
      playerBEventEntryFk: result.playerBEventEntryId,
      eligibilityRating: playerATournamentEntryInfo.eligibilityRating + playerBTournamentEntryInfo.eligibilityRating,
      seedRating: playerATournamentEntryInfo.seedRating + playerBTournamentEntryInfo.seedRating
    };
  }

  /**
   *
   * @param eventEntryId
   * @private
   */
  private findPlayerEntryInfo(eventEntryId: number): TournamentEntryInfo {
    for (const eventEntry of this.doublesEventEntries) {
      if (eventEntry.id === eventEntryId) {
        const tournamentEntryFk = eventEntry.tournamentEntryFk;
        for (const tournamentEntryInfo of this.tournamentEntryInfos) {
          if (tournamentEntryFk === tournamentEntryInfo.entryId) {
            return tournamentEntryInfo;
          }
        }
      }
    }
    return null;
  }
}
