import {AfterViewInit, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs/operators';
import {BehaviorSubject} from 'rxjs';
import {PlayerStatus} from '../model/player-status.model';
import {TournamentEntryInfo} from '../../tournament/model/tournament-entry-info.model';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {PlayerCheckinDialogComponent} from '../player-checkin-dialog/player-checkin-dialog.component';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {CheckInType} from '../../tournament/model/check-in-type.enum';
import {EventStatusCode} from '../model/event-status-code.enum';

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
  tournamentDuration: number;

  @Input()
  checkInType: CheckInType;

  @Input()
  playerStatusList: PlayerStatus[];

  @Input()
  entryInfos: TournamentEntryInfo[];

  @Input()
  tournamentEvents: TournamentEvent[];

  @Output()
  eventEmitter: EventEmitter<any> = new EventEmitter<any>();

  // map of letter to enhanced player profile list with players starting with this letter - for ALL players
  public alphabeticalPlayerStatusMap: Map<string, EnhancedPlayerStatus[]> = null;

  // statuses filtered by day or by name
  public filteredPlayerStatuses: Map<string, EnhancedPlayerStatus[]> = null;

  filterName: string;

  filterByDay: number;

  filterByEventId: number;

  // helper array for producing All, Day 1, Day 2 etc buttons
  tournamentDaysArray: number [];

  // ids of tournaments for a given day
  tournamentEventsForDay: any [] = [];

  filteredCount: number;

  // count of checked in players in the fitlered entries
  checkedInCount: number;

  private subject: BehaviorSubject<string> = new BehaviorSubject<string>('');

  constructor(private dialog: MatDialog) {
    this.filterName = '';
    this.filterByDay = 0;
    this.tournamentDaysArray = [0, 1];
    this.tournamentDuration = 1;
    this.filteredCount = 0;
    this.checkedInCount = 0;
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
    this.tournamentDaysArray = [];
    for (let day = 0; day <= this.tournamentDuration; day++) {
      this.tournamentDaysArray.push(day);
    }
    if (this.tournamentDay <= this.tournamentDuration) {
      this.filterByDay = this.tournamentDay;
    }

    if (this.entryInfos != null && this.playerStatusList != null) {
      this.prepareData();
      if (this.checkInType === CheckInType.DAILY) {
        this.onFilterByDay(this.filterByDay);
      } else {
        this.onFilterByEventId(0);
      }
    }

    if (this.tournamentEvents != null && this.tournamentEvents.length > 0) {
      if (this.checkInType === CheckInType.DAILY) {
        // separate event ids by day and put all of them also in the 0th index for All selection
        this.tournamentEventsForDay = [];
        for (const tournamentEvent of this.tournamentEvents) {
          // all events at index 0
          let allEventIds = this.tournamentEventsForDay[0] ?? [];
          allEventIds.push(tournamentEvent.id);
          this.tournamentEventsForDay[0] = allEventIds;

          // days events at day index
          const eventDay = tournamentEvent.day;
          const dayEventIds: number [] = this.tournamentEventsForDay[eventDay] ?? [];
          dayEventIds.push(tournamentEvent.id);
          this.tournamentEventsForDay[eventDay] = dayEventIds;
        }
      }
    }
  }

  prepareData() {
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

  /**
   * Filters by name
   * @param filterValue
   * @private
   */
  private filterByName(filterValue: string) {
    if (filterValue?.length > 0) {
      const letterToStatusMap = new Map<string, EnhancedPlayerStatus[]>;
      this.filteredPlayerStatuses.forEach((infosStartingAtLetter: EnhancedPlayerStatus[], firstLetter: string) => {
        const filteredList = infosStartingAtLetter.filter((enhancedPlayerStatus: EnhancedPlayerStatus) => {
          return enhancedPlayerStatus.entryInfo.firstName.includes(filterValue, 0) ||
            enhancedPlayerStatus.entryInfo.lastName.includes(filterValue, 0);
        });
        if (filteredList.length > 0) {
          letterToStatusMap.set(firstLetter, filteredList);
        }
      });
      this.filteredPlayerStatuses = letterToStatusMap;
    } else {
      // reset
      this.filteredPlayerStatuses = this.alphabeticalPlayerStatusMap;
    }
    this.countFiltered();
  }

  clearFilter() {
    this.filterName = '';
    this.onFilterChange(this.filterName);
  }

  onFilterChange(filterValue: string) {
    this.subject.next(filterValue);
  }

  /**
   * Filters by day only
   * @param tournamentDay either day 1, 2, etc. or 0 for all events
   */
  onFilterByDay(tournamentDay: number) {
    this.filterByDay = tournamentDay;
    this.filterName = '';
    let filteredPlayerStatuses = new Map<string, EnhancedPlayerStatus[]>;
    if (this.alphabeticalPlayerStatusMap.size > 0 && this.tournamentEventsForDay.length >= this.filterByDay) {
      const eventsToSelect: number [] = this.tournamentEventsForDay[this.filterByDay];
      if (eventsToSelect != null) {
        this.alphabeticalPlayerStatusMap.forEach((playerStatusForLetter, letter) => {
          let filteredPlayerStatusForLetter: EnhancedPlayerStatus [] = [];
          for (const enhancedPlayerStatus of playerStatusForLetter) {
            const playerEvents: number [] = enhancedPlayerStatus.entryInfo.eventIds;
            if (playerEvents != null) {
              for (const eventId of playerEvents) {
                if (eventsToSelect.includes(eventId, 0)) {
                  filteredPlayerStatusForLetter.push(enhancedPlayerStatus);
                  break;
                }
              }
            }
          }
          if (filteredPlayerStatusForLetter.length > 0) {
            filteredPlayerStatuses.set(letter, filteredPlayerStatusForLetter);
          }
        });
      }
      this.filteredPlayerStatuses = filteredPlayerStatuses;
      this.countFiltered();
    }
  }

  /**
   * Counts number of filtered entries
   */
  countFiltered() {
    let filteredCount = 0;
    let checkedInCount = 0;
    if (this.filteredPlayerStatuses?.size > 0) {
      this.filteredPlayerStatuses.forEach((infosStartingAtLetter: EnhancedPlayerStatus[], firstLetter) => {
        if (infosStartingAtLetter != null) {
          infosStartingAtLetter.forEach((enhancedPlayerStatus) => {
            if (enhancedPlayerStatus.entryInfo.eventIds?.length > 0) {
              filteredCount++;
            }
            if (enhancedPlayerStatus.playerStatus.eventStatusCode === EventStatusCode.WILL_PLAY) {
              checkedInCount++;
            }
          });
        }
      });
    }
    this.filteredCount = filteredCount;
    this.checkedInCount = checkedInCount;
  }

  /**
   * Filters by event ID
   */
  onFilterByEventId(eventId: number) {
    this.filterByEventId = eventId;
    let filteredCount = 0;
    let checkedInCount = 0;
    if (eventId !== 0) {
      let filteredPlayerStatuses = new Map<string, EnhancedPlayerStatus[]>;
      if (this.alphabeticalPlayerStatusMap.size > 0 && this.filterByEventId != null) {
        this.alphabeticalPlayerStatusMap.forEach((playerStatusForLetter, letter) => {
          let filteredPlayerStatusForLetter: EnhancedPlayerStatus [] = [];
          for (const enhancedPlayerStatus of playerStatusForLetter) {
            const playerEvents: number [] = enhancedPlayerStatus.entryInfo.eventIds;
            if (playerEvents != null && playerEvents.includes(this.filterByEventId, 0)) {
              filteredPlayerStatusForLetter.push(enhancedPlayerStatus);
              filteredCount++;
            }
            if (enhancedPlayerStatus.playerStatus.eventStatusCode === EventStatusCode.WILL_PLAY) {
              checkedInCount++;
            }
          }
          if (filteredPlayerStatusForLetter.length > 0) {
            filteredPlayerStatuses.set(letter, filteredPlayerStatusForLetter);
          }
        });
      }
      this.filteredPlayerStatuses = filteredPlayerStatuses;
      this.filteredCount = filteredCount;
      this.checkedInCount = checkedInCount;
    } else {
      this.filteredPlayerStatuses = this.alphabeticalPlayerStatusMap;
      this.countFiltered();
    }
  }
}

export class EnhancedPlayerStatus {
  playerStatus: PlayerStatus;
  entryInfo: TournamentEntryInfo;
}
