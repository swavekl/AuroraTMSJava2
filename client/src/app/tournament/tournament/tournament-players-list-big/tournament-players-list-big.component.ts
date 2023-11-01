import {Component, EventEmitter, Input, OnChanges, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';
import {state} from '@angular/animations';
import {RecordSearchData} from '../../../profile/usatt-record-search-popup/usatt-record-search-popup.component';
import {UsattRecordSearchCallbackData} from '../../../profile/service/usatt-record-search-popup.service';

@Component({
  selector: 'app-tournament-players-list-big',
  templateUrl: './tournament-players-list-big.component.html',
  styleUrls: ['./tournament-players-list-big.component.scss']
})
export class TournamentPlayersListBigComponent implements OnChanges {

  @Input()
  public entryInfos: TournamentEntryInfo[]= [];

  @Input()
  public tournamentName: string;

  @Input()
  public tournamentId: number;

  @Input()
  public tournamentReady: boolean;

  @Output()
  public viewEntry: EventEmitter<TournamentEntryInfo> = new EventEmitter<TournamentEntryInfo>();

  @Output()
  public addEntry: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  public findPlayer: EventEmitter<any> = new EventEmitter<any>();

  // list of entries divided by
  alphabeticalEntryInfos: Map<string, TournamentEntryInfo[]> = null;

  validEntriesCount: number;

  ngOnChanges(changes: SimpleChanges): void {
    const entryInfoChanges: SimpleChange = changes.entryInfos;
    if (entryInfoChanges != null) {
      const entryInfos = entryInfoChanges.currentValue;
      if (entryInfos != null) {
        // sort them so we don't have to sort the map
        entryInfos.sort((entry1: TournamentEntryInfo, entry2: TournamentEntryInfo) => {
          const name1 = entry1.lastName + " " + entry1.firstName;
          const name2 = entry2.lastName + " " + entry2.firstName;
          return name1.localeCompare(name2);
        });

        // count entries which have events - i.e. not withdrawn
        let count = 0;
        entryInfos.forEach((entry: TournamentEntryInfo) => {
          if (entry.eventIds?.length > 0 || entry.waitingListEventIds?.length > 0) {
            count++;
          }
        });

        // group entries by letter so it is easier to locate them
        const letterToEntriesMap = new Map<string, TournamentEntryInfo[]>;
        entryInfos.map((entryInfo: TournamentEntryInfo) => {
          const firstLetter: string = (entryInfo.lastName != null) ? entryInfo.lastName.charAt(0) : null;
          if (firstLetter != null) {
            let infosStartingAtLetter: TournamentEntryInfo[] = letterToEntriesMap.get(firstLetter);
            if (infosStartingAtLetter == null) {
              infosStartingAtLetter = [];
              letterToEntriesMap.set(firstLetter, infosStartingAtLetter);
            }
            infosStartingAtLetter.push(entryInfo);
          }
        });

        this.validEntriesCount = count;
        this.entryInfos = entryInfos;
        this.alphabeticalEntryInfos = letterToEntriesMap;
      }
    }
  }

  onViewEntry(entryInfo: TournamentEntryInfo) {
    this.viewEntry.emit(entryInfo);
  }

  onRegisterPlayer() {
    this.addEntry.emit(null);
  }

  isTournamentReady() {
    return this.tournamentReady;
  }

  showPlayerTooltip(firstName: string, lastName: string): boolean {
    const fullName = this.getPlayerTooltipText(firstName, lastName);
    return fullName.length > 24;
  }

  getPlayerTooltipText(firstName: string, lastName: string): string {
     return `${lastName}, ${firstName}`;
  }

    protected readonly state = state;

  onFindPlayer() {
    this.findPlayer.emit(null);
  }

  hasEvents(entryInfo: TournamentEntryInfo): boolean {
    return (entryInfo.eventIds?.length > 0) || (entryInfo.waitingListEventIds?.length > 0);
  }
}

