import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {PrizeInfo} from '../../tournament/tournament-config/model/prize-info.model';

@Component({
  selector: 'app-prize-list',
  templateUrl: './prize-list.component.html',
  styleUrls: ['./prize-list.component.scss']
})
export class PrizeListComponent implements OnInit, OnChanges {

  @Input()
  events: TournamentEvent[];

  selectedEvent: TournamentEvent;

  prizeDataList: PrizeData [] = [];

  completionStatus: any = {};

  tournamentCurrency: 'USD';

  // displayedColumns: string[] = ['place', 'playerFullName', 'prizeMoneyAmount', 'awardTrophy'];
  displayedColumns: string[] = ['place', 'playerFullName', 'awards'];
  private allEventsPrizeData: any = {};

  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const eventsChanges: SimpleChange = changes.events;
    if (eventsChanges && eventsChanges.currentValue != null) {
      const events = eventsChanges.currentValue;
      if (events.length > 0) {
        this.events = events;
        this.prepareAllEventPrizeData();
      }
    }
  }

  onSelectEvent(tournamentEvent: TournamentEvent) {
    this.selectedEvent = tournamentEvent;
    if (this.selectedEvent) {
      this.prizeDataList = this.allEventsPrizeData[this.selectedEvent.id];
    }
  }

  isSelected(tournamentEvent: TournamentEvent): boolean {
    return tournamentEvent.id === this.selectedEvent?.id;
  }

  isCompletedEvent(tournamentEvent: TournamentEvent) {
    return this.completionStatus[tournamentEvent.id];
  }

  /**
   *
   * @private
   */
  private prepareAllEventPrizeData() {
    if (this.events) {
      this.events.forEach((tournamentEvent: TournamentEvent) => {
        const eventPrizeData = this.prepareOneEventPrizeData(tournamentEvent);
        this.allEventsPrizeData[tournamentEvent.id] = eventPrizeData;
        const isCompleted = this.determineEventCompletionStatus(eventPrizeData);
        this.completionStatus[tournamentEvent.id] = isCompleted;
      });
    }
  }

  /**
   *
   * @param tournamentEvent
   * @private
   */
  private prepareOneEventPrizeData(tournamentEvent: TournamentEvent) {
    let prizeInfoList = tournamentEvent.configuration?.prizeInfoList ?? [];
    prizeInfoList = (prizeInfoList.length > 0) ? prizeInfoList : this.prepareDefaultPrizeInfos();
    const finalPlayerRankings = tournamentEvent.configuration?.finalPlayerRankings ?? {};
    const prizeDataList = [];
    prizeInfoList.forEach((prizeInfo: PrizeInfo) => {
      const placeEnd = (prizeInfo.awardedForPlaceRangeEnd === 0)
        ? prizeInfo.awardedForPlace : prizeInfo.awardedForPlaceRangeEnd;
      for (let place = prizeInfo.awardedForPlace; place <= placeEnd; place++) {
        const playerFullName = finalPlayerRankings[place];
        const prizeData: PrizeData = {
          place: place,
          prizeMoneyAmount: prizeInfo.prizeMoneyAmount,
          awardTrophy: prizeInfo.awardTrophy,
          playerFullName: playerFullName
        };
        prizeDataList.push(prizeData);
      }
    });
    return prizeDataList;
  }

  /**
   * In case they didn't have time to configure the prize infos assume 3 trophies are awarded so we can show the first 3 places
   * @private
   */
  private prepareDefaultPrizeInfos() {
    const defaultPrizeInfoList = [];
    defaultPrizeInfoList.push({division: 'A', awardedForPlace: 1, awardedForPlaceRangeEnd: 0, prizeMoneyAmount: 0, awardTrophy: true});
    defaultPrizeInfoList.push({division: 'A', awardedForPlace: 2, awardedForPlaceRangeEnd: 0, prizeMoneyAmount: 0, awardTrophy: true});
    defaultPrizeInfoList.push({division: 'A', awardedForPlace: 3, awardedForPlaceRangeEnd: 4, prizeMoneyAmount: 0, awardTrophy: true});
    return defaultPrizeInfoList;
  }

  /**
   *
   * @param eventPrizeData
   * @private
   */
  private determineEventCompletionStatus(eventPrizeData: PrizeData[]): boolean {
    let allPlayersDetermined = eventPrizeData?.length > 0;
    eventPrizeData.forEach((prizeData: PrizeData) => {
      const playerDetermined = (prizeData.playerFullName != null && prizeData.playerFullName !== '');
      allPlayersDetermined = allPlayersDetermined && playerDetermined;
    });
    return allPlayersDetermined;


  }
}

export class PrizeData {
  place: number;
  prizeMoneyAmount: number;
  awardTrophy: boolean;
  playerFullName: string;
}
