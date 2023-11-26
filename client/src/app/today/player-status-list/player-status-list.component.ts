import {AfterViewInit, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs/operators';
import {BehaviorSubject} from 'rxjs';
import {PlayerStatus} from '../model/player-status.model';
import {TournamentEntryInfo} from '../../tournament/model/tournament-entry-info.model';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {PlayerCheckinDialogComponent} from '../player-checkin-dialog/player-checkin-dialog.component';

@Component({
  selector: 'app-player-status-list',
  templateUrl: './player-status-list.component.html',
  styleUrls: ['./player-status-list.component.scss']
})
export class PlayerStatusListComponent implements OnChanges, AfterViewInit {

  @Input()
  tournamentId: number;

  @Input()
  tournamentName: string;

  @Input()
  tournamentDay: number;

  @Input()
  playerStatusList: PlayerStatus[];

  @Input()
  entryInfos: TournamentEntryInfo[];

  @Output()
  eventEmitter: EventEmitter<any> = new EventEmitter<any>();

  // map of letter to enhanced player profile list with players starting with this letter
  public alphabeticalPlayerStatusMap: Map<string, EnhancedPlayerStatus[]> = null;

  filterName: string;

  private subject: BehaviorSubject<string> = new BehaviorSubject<string>('');

  constructor(private dialog: MatDialog) {
    this.filterName = '';
  }

  onViewStatus(enhancedPlayerStatus: EnhancedPlayerStatus) {
    const fullPlayerName = `${enhancedPlayerStatus.entryInfo.lastName}, ${enhancedPlayerStatus.entryInfo.firstName}`;
    const config: MatDialogConfig = {
      width: '350px', height: '565px', data: {
        playerStatus: enhancedPlayerStatus.playerStatus,
        fullName: fullPlayerName,
        tournamentDay: this.tournamentDay,
        eventName: ''
      }
    };
    // save the scope because it is wiped out in the component
    // so that it is not sent into the http service
    // const callbackScope = callbackData.callbackScope;
    const dialogRef = this.dialog.open(PlayerCheckinDialogComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'ok') {
        this.eventEmitter.emit({action: 'ok', playerStatus: result.playerStatus});
      }
    });
  }

  showPlayerTooltip(firstName: string, lastName: string): boolean {
    const fullName = this.getPlayerTooltipText(firstName, lastName);
    return fullName.length > 24;
  }

  getPlayerTooltipText(firstName: string, lastName: string): string {
    return `${lastName}, ${firstName}`;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.entryInfos != null && this.playerStatusList != null) {
      this.prepareData();
    }
  }

  prepareData () {
    // sort them so we don't have to sort the map
    this.entryInfos.sort((entry1: TournamentEntryInfo, entry2: TournamentEntryInfo) => {
      const name1 = entry1.lastName + ' ' + entry1.firstName;
      const name2 = entry2.lastName + ' ' + entry2.firstName;
      return name1.localeCompare(name2);
    });

    const letterToStatusMap = new Map<string, EnhancedPlayerStatus[]>;
    this.entryInfos.map((entryInfo: TournamentEntryInfo) => {
      const firstLetter: string = (entryInfo.lastName != null) ? entryInfo.lastName.charAt(0) : null;
      if (firstLetter != null) {
        let infosStartingAtLetter: EnhancedPlayerStatus[] = letterToStatusMap.get(firstLetter);
        if (infosStartingAtLetter == null) {
          infosStartingAtLetter = [];
          letterToStatusMap.set(firstLetter, infosStartingAtLetter);
        }
        let foundPlayerStatus = null;
        if (this.playerStatusList != null) {
          for (let i = 0; i < this.playerStatusList.length; i++) {
            const playerStatus: PlayerStatus = this.playerStatusList[i];
            if (playerStatus.playerProfileId === entryInfo.profileId && playerStatus.tournamentId === this.tournamentId) {
              foundPlayerStatus = playerStatus;
              // console.log('Found player status for ' + entryInfo.lastName + ', ' + entryInfo.firstName + ' -> ' + playerStatus.eventStatusCode + ' tournamentid '+ playerStatus.tournamentId);
              break;
            }
          }
        }
        if (foundPlayerStatus == null) {
          foundPlayerStatus = new PlayerStatus();
          foundPlayerStatus.playerProfileId = entryInfo.profileId;
          foundPlayerStatus.tournamentId = this.tournamentId;
        }
        let eei: EnhancedPlayerStatus = new EnhancedPlayerStatus();
        eei.entryInfo = entryInfo;
        eei.playerStatus = foundPlayerStatus;
        infosStartingAtLetter.push(eei);
      }
    });
    this.alphabeticalPlayerStatusMap = letterToStatusMap;
  }

  ngAfterViewInit(): void {
      this.subject
        .pipe(
          skip(1),
          distinctUntilChanged(),
          debounceTime(500)
        )
        .subscribe((filterValue: string) => {
          this.filterByName(filterValue);
        });
  }

  private filterByName(filterValue: string) {
    if (filterValue?.length > 0) {
      const letterToStatusMap = new Map<string, EnhancedPlayerStatus[]>;
      this.alphabeticalPlayerStatusMap.forEach((infosStartingAtLetter: EnhancedPlayerStatus[], firstLetter: string) => {
        const filteredList = infosStartingAtLetter.filter((enhancedPlayerStatus: EnhancedPlayerStatus) => {
          return enhancedPlayerStatus.entryInfo.firstName.includes(filterValue, 0) ||
            enhancedPlayerStatus.entryInfo.lastName.includes(filterValue, 0);
        });
        if (filteredList.length > 0) {
          letterToStatusMap.set(firstLetter, filteredList);
        }
      });
      this.alphabeticalPlayerStatusMap = letterToStatusMap;
    } else {
      // reset
      this.prepareData();
    }
  }

  clearFilter() {
    this.filterName = '';
    this.onFilterChange(this.filterName);
  }

  onFilterChange(filterValue: string) {
    this.subject.next(filterValue);
  }
}

export class EnhancedPlayerStatus {
  playerStatus: PlayerStatus;
  entryInfo: TournamentEntryInfo;
}
