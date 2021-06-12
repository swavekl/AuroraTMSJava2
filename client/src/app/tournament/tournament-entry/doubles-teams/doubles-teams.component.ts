import {AfterViewInit, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {DoublesPair} from '../model/doubles-pair.model';
import {TournamentEventEntry} from '../model/tournament-event-entry.model';
import {DoublesPairInfo} from '../model/doubles-pair-info.model';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {DoublesPairDialogComponent, DoublesPairingData, DoublesPairingInfo} from '../doubles-pair-dialog/doubles-pair-dialog.component';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';
import {MatTableDataSource} from '@angular/material/table';

@Component({
  selector: 'app-doubles-teams',
  templateUrl: './doubles-teams.component.html',
  styleUrls: ['./doubles-teams.component.scss']
})
export class DoublesTeamsComponent implements OnInit, OnChanges {

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

  displayedColumns: string[] = ['index', 'playerAName', 'playerARating', 'playerBName', 'playerBRating', 'combinedRating', 'breakPair'];

  dataSource: MatTableDataSource<DoublesPairInfo>;

  eventMaxRating: number;

  constructor(public dialog: MatDialog) {
    this.dataSource = new MatTableDataSource(this.doublesPairInfos);
    this.eventMaxRating = 0;
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const doublesPairInfosChange: SimpleChange = changes.doublesPairInfos;
    if (doublesPairInfosChange) {
      const doublesPairInfos = doublesPairInfosChange.currentValue;
      if (doublesPairInfos) {
        this.dataSource = new MatTableDataSource(this.doublesPairInfos);
      }
    }
  }

  onSelectEvent(doublesEvent: TournamentEvent) {
    this.selectedDoublesEventId = doublesEvent.id;
    this.eventMaxRating = doublesEvent.maxPlayerRating;
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
    if (dialogData) {
      const config: MatDialogConfig = {
        width: '360px', height: '360px', data: dialogData
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
  }

  /**
   * Finds unpaired players and makes information about them
   * @private
   */
  private makeDialogData() {
    // check that data is available
    if (!this.doublesPairInfos || !this.doublesEventEntries || !this.tournamentEntryInfos) {
      return null;
    }

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
            const playerName = tournamentEntryInfo.lastName + ', ' + tournamentEntryInfo.firstName;
            const doublesPairingInfo: DoublesPairingInfo = {
              eventEntryId: eventEntry.id,
              playerName: playerName,
              playerRating: tournamentEntryInfo.eligibilityRating
            };
            doublesPairingInfos.push(doublesPairingInfo);
            break;
          }
        }
      }
    }

    const dialogData: DoublesPairingData = {
      doublesPairingInfos: doublesPairingInfos,
      eventMaxRating: this.eventMaxRating
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
