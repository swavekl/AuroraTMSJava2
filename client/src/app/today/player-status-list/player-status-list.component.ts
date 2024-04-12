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
import {PlayerStatusPipe} from '../pipe/player-status.pipe';

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

  public isFiltering$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

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
    let playerStatusToEdit = null;
    let eventName = ''
    if (this.checkInType === CheckInType.DAILY) {
      for (const playerStatus of enhancedPlayerStatus.playerStatus) {
        if (playerStatus.tournamentDay === this.tournamentDay) {
          playerStatusToEdit = playerStatus;
          break;
        }
      }
      if (playerStatusToEdit == null) {
        playerStatusToEdit = new PlayerStatus();
        playerStatusToEdit.playerProfileId = enhancedPlayerStatus.entryInfo.profileId;
        playerStatusToEdit.tournamentDay = this.tournamentDay;
        playerStatusToEdit.tournamentId = this.tournamentId;
        playerStatusToEdit.eventId = 0;
      }
    } else {
      for (const playerStatus of enhancedPlayerStatus.playerStatus) {
        if (playerStatus.eventId === this.filterByEventId) {
          playerStatusToEdit = playerStatus;
          break;
        }
      }
      if (playerStatusToEdit == null) {
        playerStatusToEdit = new PlayerStatus();
        playerStatusToEdit.playerProfileId = enhancedPlayerStatus.entryInfo.profileId;
        playerStatusToEdit.tournamentDay = this.tournamentDay;
        playerStatusToEdit.tournamentId = this.tournamentId;
        playerStatusToEdit.eventId = this.filterByEventId;
      }
      for (const tournamentEvent of this.tournamentEvents) {
        if (tournamentEvent.id === this.filterByEventId) {
          eventName = tournamentEvent.name;
          break;
        }
      }
    }
    const config: MatDialogConfig = {
      width: '350px', height: '565px', data: {
        playerStatus: playerStatusToEdit,
        fullName: fullPlayerName,
        tournamentDay: this.tournamentDay,
        eventName: eventName
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

        // find this player status in case he has more checkins
        let foundEei: EnhancedPlayerStatus = null;
        for (let j = 0; j < infosStartingAtLetter.length; j++) {
          const eei: EnhancedPlayerStatus = infosStartingAtLetter[j];
          if (eei.entryInfo.profileId === entryInfo.profileId) {
            foundEei = eei;
            break;
          }
        }

        if (foundEei == null) {
          foundEei = new EnhancedPlayerStatus();
          foundEei.entryInfo = entryInfo;
          foundEei.playerStatus = [];
          infosStartingAtLetter.push(foundEei);
        }

        let foundPlayerStatus = null;
        if (this.playerStatusList != null) {
          for (let i = 0; i < this.playerStatusList.length; i++) {
            const playerStatus: PlayerStatus = this.playerStatusList[i];
            if (playerStatus.playerProfileId === entryInfo.profileId && playerStatus.tournamentId === this.tournamentId) {
              foundPlayerStatus = playerStatus;
              // console.log('Found player status for ' + entryInfo.lastName + ', ' + entryInfo.firstName + ' -> ' + playerStatus.eventStatusCode + ' tournamentid '+ playerStatus.tournamentId);
              foundEei.playerStatus.push(foundPlayerStatus);
            }
          }
        }
        if (foundPlayerStatus == null) {
          foundPlayerStatus = new PlayerStatus();
          foundPlayerStatus.playerProfileId = entryInfo.profileId;
          foundPlayerStatus.tournamentId = this.tournamentId;
          foundPlayerStatus.tournamentDay = 1;
          foundEei.playerStatus.push(foundPlayerStatus);
        }
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
    this.isFiltering$.next(true);
    if (filterValue?.length > 0) {
      const lcFilterValue = filterValue.toLowerCase();
      const letterToStatusMap = new Map<string, EnhancedPlayerStatus[]>;
      this.filteredPlayerStatuses.forEach((infosStartingAtLetter: EnhancedPlayerStatus[], firstLetter: string) => {
        const filteredList = infosStartingAtLetter.filter((enhancedPlayerStatus: EnhancedPlayerStatus) => {
          return enhancedPlayerStatus.entryInfo.firstName.toLowerCase().includes(lcFilterValue, 0) ||
            enhancedPlayerStatus.entryInfo.lastName.toLowerCase().includes(lcFilterValue, 0);
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

    if (this.checkInType === CheckInType.DAILY) {
      this.countFilteredByDay();
    } else {
      this.countFilteredByEvent(this.filterByEventId);
    }
    this.isFiltering$.next(false);
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
    this.isFiltering$.next(true);
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
      this.countFilteredByDay();
    }
    this.isFiltering$.next(false);
  }

  /**
   * Counts number of filtered entries
   */
  countFilteredByDay() {
    let filteredCount = 0;
    let checkedInCount = 0;
    if (this.filteredPlayerStatuses?.size > 0) {
      this.filteredPlayerStatuses.forEach((infosStartingAtLetter: EnhancedPlayerStatus[], firstLetter) => {
        if (infosStartingAtLetter != null) {
          infosStartingAtLetter.forEach((enhancedPlayerStatus) => {
            if (enhancedPlayerStatus.entryInfo.eventIds?.length > 0) {
              filteredCount++;
            }
            for (const playerStatus of enhancedPlayerStatus.playerStatus) {
              if (playerStatus.eventStatusCode === EventStatusCode.WILL_PLAY)
                if (playerStatus.tournamentDay === this.tournamentDay || this.tournamentDay === 0) {
                  checkedInCount++;
              }
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
    this.isFiltering$.next(true);
    this.filterByEventId = eventId;
    if (eventId !== 0) {
      let filteredPlayerStatuses = new Map<string, EnhancedPlayerStatus[]>;
      if (this.alphabeticalPlayerStatusMap.size > 0 && this.filterByEventId != null) {
        this.alphabeticalPlayerStatusMap.forEach((playerStatusForLetter, letter) => {
          let filteredPlayerStatusForLetter: EnhancedPlayerStatus [] = [];
          for (const enhancedPlayerStatus of playerStatusForLetter) {
            const playerEvents: number [] = enhancedPlayerStatus.entryInfo.eventIds;
            if (playerEvents != null && playerEvents.includes(this.filterByEventId, 0)) {
              filteredPlayerStatusForLetter.push(enhancedPlayerStatus);
            }
          }
          if (filteredPlayerStatusForLetter.length > 0) {
            filteredPlayerStatuses.set(letter, filteredPlayerStatusForLetter);
          }
        });
      }
      this.filteredPlayerStatuses = filteredPlayerStatuses;
    } else {
      this.filteredPlayerStatuses = this.alphabeticalPlayerStatusMap;
    }
    this.countFilteredByEvent(eventId);
    this.isFiltering$.next(false);
  }

  private countFilteredByEvent(eventId: number) {
    let filteredCount = 0;
    let checkedInCount = 0;
    if (this.filteredPlayerStatuses?.size > 0) {
      this.filteredPlayerStatuses.forEach((infosStartingAtLetter: EnhancedPlayerStatus[], firstLetter) => {
        if (infosStartingAtLetter != null) {
          infosStartingAtLetter.forEach((enhancedPlayerStatus) => {
            const playerEvents: number [] = enhancedPlayerStatus.entryInfo.eventIds;
            if (playerEvents != null && playerEvents.includes(eventId, 0)) {
              filteredCount++;
            }
            for (const playerStatus of enhancedPlayerStatus.playerStatus) {
              if (playerStatus.eventStatusCode === EventStatusCode.WILL_PLAY && playerStatus.eventId === eventId) {
                  checkedInCount++;
              }
            }
          });
        }
      });
    }
    this.filteredCount = filteredCount;
    this.checkedInCount = checkedInCount;
  }

  getStatusTooltip(playerStatus: PlayerStatus) {
    let reason = '';
      switch (playerStatus.eventStatusCode) {
        case EventStatusCode.WILL_NOT_PLAY:
          reason = new PlayerStatusPipe().transform(playerStatus.eventStatusCode, playerStatus.estimatedArrivalTime);
          reason += '. ' + (playerStatus.reason ? playerStatus.reason : '');
          break;
        case EventStatusCode.WILL_PLAY_BUT_IS_LATE:
          reason = new PlayerStatusPipe().transform(playerStatus.eventStatusCode, playerStatus.estimatedArrivalTime);
          break;

        case EventStatusCode.WILL_PLAY:
          reason = new PlayerStatusPipe().transform(playerStatus.eventStatusCode, playerStatus.estimatedArrivalTime);
          break;
      }
      if (this.checkInType == CheckInType.DAILY) {
        if (playerStatus.eventStatusCode != null) {
          reason = `Day ${playerStatus.tournamentDay}: ${reason}`;
        }
      } else {
        let eventName = 'Event'
        for (const tournamentEvent of this.tournamentEvents) {
          if (playerStatus.eventId === tournamentEvent.id) {
            eventName = tournamentEvent.name;
            break;
          }
        }
        reason = `${eventName}: ${reason}`;
      }
    return reason;
  }
}

export class EnhancedPlayerStatus {
  playerStatus: PlayerStatus[];  // statuses for each day or event
  entryInfo: TournamentEntryInfo;
}
