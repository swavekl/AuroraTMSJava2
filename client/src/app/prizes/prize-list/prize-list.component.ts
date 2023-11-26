import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {PrizeInfo} from '../../tournament/tournament-config/model/prize-info.model';
import {DrawMethod} from '../../tournament/tournament-config/model/draw-method.enum';
import {MatchCard} from '../../matches/model/match-card.model';

@Component({
  selector: 'app-prize-list',
  templateUrl: './prize-list.component.html',
  styleUrls: ['./prize-list.component.scss']
})
export class PrizeListComponent implements OnInit, OnChanges {

  @Input()
  events: TournamentEvent[];

  @Input()
  finishedRRMatchCards: MatchCard[] = [];

  selectedEvent: TournamentEvent;

  prizeDataList: PrizeData [] = [];

  completionStatus: any = {};

  tournamentCurrency: 'USD';

  // map of event id to prize data list for all of this event's divisions
  divisionsPrizeDataList: Map<number, PrizeData []> = new Map<number, PrizeData []>();

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
    // const matchCardsChanges: SimpleChange = changes.finishedRRMatchCards;
    // if (matchCardsChanges && matchCardsChanges.currentValue != null) {
    //   this.finishedRRMatchCards = matchCardsChanges.currentValue;
    // }

    if (this.events != null && this.finishedRRMatchCards != null) {
      this.prepareRoundRobinPrizeData();
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
        let isCompleted = true;
        if (tournamentEvent.drawMethod !== DrawMethod.DIVISION) {
          isCompleted = this.determineEventCompletionStatus(eventPrizeData);
        }
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
    prizeInfoList = (prizeInfoList.length > 0) ? prizeInfoList : this.prepareDefaultPrizeInfos('A');
    const finalPlayerRankings = tournamentEvent.configuration?.finalPlayerRankings ?? {};
    const prizeDataList = [];
    prizeInfoList.forEach((prizeInfo: PrizeInfo) => {
      const placeEnd = (prizeInfo.awardedForPlaceRangeEnd === 0)
        ? prizeInfo.awardedForPlace : prizeInfo.awardedForPlaceRangeEnd;
      for (let place = prizeInfo.awardedForPlace; place <= placeEnd; place++) {
        const playerFullName = finalPlayerRankings[place];
        const prizeData: PrizeData = {
          division: '',
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
  private prepareDefaultPrizeInfos(division: string) {
    const defaultPrizeInfoList = [];
    defaultPrizeInfoList.push({division: division, awardedForPlace: 1, awardedForPlaceRangeEnd: 0, prizeMoneyAmount: 0, awardTrophy: true});
    defaultPrizeInfoList.push({division: division, awardedForPlace: 2, awardedForPlaceRangeEnd: 0, prizeMoneyAmount: 0, awardTrophy: true});
    defaultPrizeInfoList.push({division: division, awardedForPlace: 3, awardedForPlaceRangeEnd: 4, prizeMoneyAmount: 0, awardTrophy: true});
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

  /**
   * Prepares prize & players information for all groups in giant round robin event type where players don't advance further
   * @private
   */
  private prepareRoundRobinPrizeData() {
    const divisionsPrizeDataList: Map<number, PrizeData []> = new Map<number, PrizeData []>();
    this.events.forEach((tournamentEvent: TournamentEvent) => {
      if (tournamentEvent.drawMethod === DrawMethod.DIVISION && tournamentEvent.playersToAdvance === 0) {
        const rrPrizeData: PrizeData [] = [];
        const prizeInfoList = tournamentEvent.configuration?.prizeInfoList ?? [];
        this.finishedRRMatchCards.forEach((matchCard: MatchCard) => {
          if (matchCard.eventFk === tournamentEvent.id) {
            const playerRankingsJSON = matchCard?.playerRankings;
            if (playerRankingsJSON && playerRankingsJSON?.length > 0) {
              const playerRankings = JSON.parse(playerRankingsJSON);
              if (Object.keys(playerRankings).length > 0) {
                const groupPrizeInfo = this.getRRGroupPrizeInfo(prizeInfoList, matchCard, tournamentEvent);
                const profileIdToNameMap = matchCard?.profileIdToNameMap;
                for (const [rank, playerProfileId] of Object.entries(playerRankings)) {
                  const playerFullName = profileIdToNameMap[playerProfileId as string];
                  const place = Number(rank);
                  const placePrizeInfoList = groupPrizeInfo.filter((prizeInfo: PrizeInfo) => {
                    const placeEnd = (prizeInfo.awardedForPlaceRangeEnd === 0)
                      ? prizeInfo.awardedForPlace : prizeInfo.awardedForPlaceRangeEnd;
                    return (place >= prizeInfo.awardedForPlace) && (place <= placeEnd);
                  });
                  if (placePrizeInfoList.length > 0) {
                    const prizeInfo = placePrizeInfoList[0];
                    const division = this.getRRDivisionName(matchCard, tournamentEvent);
                    rrPrizeData.push({
                      division: division,
                      place: place,
                      prizeMoneyAmount: prizeInfo.prizeMoneyAmount,
                      awardTrophy: prizeInfo.awardTrophy,
                      playerFullName: playerFullName
                    });
                  }
                }
              }
            }
          }
        });
        divisionsPrizeDataList.set(tournamentEvent.id, rrPrizeData);
      }
    });
    this.divisionsPrizeDataList = divisionsPrizeDataList;
  }

  /**
   * gets RR division prize infos if configured
   * @param prizeInfoList
   * @param matchCard
   * @private
   */
  private getRRGroupPrizeInfo(prizeInfoList: PrizeInfo[], matchCard: MatchCard, tournamentEvent: TournamentEvent) {
    const matchCardDivision = this.getRRDivisionName(matchCard, tournamentEvent);
    let divisionPrizeInfoList: PrizeInfo [] = prizeInfoList.filter(
      (prizeInfo: PrizeInfo) => {
        return prizeInfo.division === matchCardDivision;
      });
    if (divisionPrizeInfoList.length === 0) {
      divisionPrizeInfoList = this.prepareDefaultPrizeInfos(matchCardDivision);
    }
    divisionPrizeInfoList.sort((a, b) => {
      return (a.awardedForPlace === b.awardedForPlace) ? 0 :
        (a.awardedForPlace < b.awardedForPlace ? -1 : 1);
    });
    return divisionPrizeInfoList;
  }

  isGiantRREvent(tournamentEvent: TournamentEvent): boolean {
    // console.log('isGiantRREvent', (tournamentEvent.drawMethod === DrawMethod.DIVISION && tournamentEvent.playersToAdvance === 0));
    return tournamentEvent.drawMethod === DrawMethod.DIVISION && tournamentEvent.playersToAdvance === 0;
  }

  getRRDivisions(tournamentEvent: TournamentEvent) {
    let divisions: string [] = [];
    if (this.isGiantRREvent(tournamentEvent)) {
      const prizeInfoList = tournamentEvent.configuration.prizeInfoList;
      if (prizeInfoList && prizeInfoList.length > 0) {
        divisions = this.getDivisionsNameFromPrizeInfos(tournamentEvent);
      } else {
        if (this.finishedRRMatchCards != null && this.finishedRRMatchCards.length > 0) {
          this.finishedRRMatchCards.forEach((matchCard: MatchCard) => {
            divisions.push(this.getRRDivisionName(matchCard, tournamentEvent));
          });
        }
      }
    }
    return divisions;
  }

  /**
   *
   * @param tournamentEvent
   */
  getDivisionsNameFromPrizeInfos(tournamentEvent: TournamentEvent) {
    let divisions: string [] = [];
    const prizeInfoList = tournamentEvent.configuration.prizeInfoList;
    prizeInfoList.forEach((prizeInfo: PrizeInfo) => {
      const divisionName = prizeInfo.division;
      if (divisions.indexOf(divisionName) === -1) {
        divisions.push(divisionName);
      }
    });
    divisions = divisions.sort((div1: string, div2: string) => {
      return div1.localeCompare(div2);
    });
    return divisions;
  }

  isCompletedRRDivision(tournamentEvent: TournamentEvent, division: string): boolean {
    let isCompleted = false;
    if (this.finishedRRMatchCards != null && this.finishedRRMatchCards.length > 0) {
      this.finishedRRMatchCards.forEach((matchCard: MatchCard) => {
        if (this.getRRDivisionName(matchCard, tournamentEvent) === division) {
          isCompleted = matchCard.playerRankings != null;
        }
      });
    }
    return isCompleted;
  }

  private getRRDivisionName(matchCard: MatchCard, tournamentEvent: TournamentEvent): string {
    let firstDivisionName = 'A'.charCodeAt(0)
    const divisionsNameFromPrizeInfos = this.getDivisionsNameFromPrizeInfos(tournamentEvent);
    if (divisionsNameFromPrizeInfos && divisionsNameFromPrizeInfos.length > 0) {
      const strDivisionName = divisionsNameFromPrizeInfos[0];
      if (strDivisionName?.length > 0) {
        firstDivisionName = strDivisionName[0].charCodeAt(0);
      }
    }
    return String.fromCharCode(firstDivisionName + (matchCard.groupNum - 1));
  }

  getRRDivisionPrizeData(tournamentEvent: TournamentEvent, division: string): PrizeData [] {
    let divisionPrizeData: PrizeData [] = [];
    // find all divisions prize data for this event
    this.divisionsPrizeDataList.forEach((eventPrizeDataList: PrizeData[], eventId: number) => {
      if (tournamentEvent.id === eventId) {
        // get the prize data only for this division
        divisionPrizeData = eventPrizeDataList.filter((prizeData: PrizeData) => {
          // console.log(division + ' divisionPrizeData ', (prizeData.division === division));
          return (prizeData.division === division);
        });
      }
    });
    return divisionPrizeData;
  }
}

export class PrizeData {
  division: string;
  place: number;
  prizeMoneyAmount: number;
  awardTrophy: boolean;
  playerFullName: string;
}
