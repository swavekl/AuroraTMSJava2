import {Component, EventEmitter, Input, OnChanges, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';

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

  @Output()
  public viewEntry: EventEmitter<TournamentEntryInfo> = new EventEmitter<TournamentEntryInfo>();

  @Output()
  public addEntry: EventEmitter<any> = new EventEmitter<any>();

  // list of entries divided by
  alphabeticalEntryInfos: Map<string, TournamentEntryInfo[]> = null;


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
        this.entryInfos = entryInfos;

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

        // sort the map by letter
        // const sortedMap = new Map([...letterToEntriesMap]
        //   .sort(([k1, v1], [k2, v2])=> {
        //   return k1.localeCompare(k2);
        // }));

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
}
