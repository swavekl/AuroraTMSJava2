import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {PrizeInfo} from '../../tournament/tournament-config/model/prize-info.model';
import {DrawMethod} from '../../tournament/tournament-config/model/draw-method.enum';
import {MatchCard} from '../../matches/model/match-card.model';

@Component({
  selector: 'app-prize-list',
  templateUrl: './prize-list.component.html',
  styleUrls: ['./prize-list.component.scss'],
  standalone: false
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

  // Explicit tracking dictionary to trigger template warning indicators independently of fallback generation
  isPrizeInfoMissing: Map<number, boolean> = new Map<number, boolean>();

  groupedEvents: { day: number, events: TournamentEvent[] }[] = [];

  divisionsPrizeDataList: Map<number, PrizeData []> = new Map<number, PrizeData []>();

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
        this.events = this.sortEventsByStartDayAndTime(events);
        this.groupEventsByDay();
        this.prepareAllEventPrizeData();
      }
    }

    if (this.events != null && this.finishedRRMatchCards != null) {
      this.prepareRoundRobinPrizeData();
    }
  }

  sortEventsByStartDayAndTime(tournamentEvents: TournamentEvent[]): TournamentEvent[] {
    return tournamentEvents.sort((eventA: TournamentEvent, eventB: TournamentEvent) => {
      const diff = eventA.day - eventB.day;
      if (diff != 0) {
        return diff > 0 ? 1 : -1;
      } else {
        const diffTime = eventA.startTime - eventB.startTime;
        if (diffTime === 0) {
          return 0;
        } else {
          return diffTime > 0 ? 1 : -1;
        }
      }
    });
  }

  private groupEventsByDay() {
    const map = new Map<number, TournamentEvent[]>();

    this.events.forEach(event => {
      const day = event.day || 1;
      if (!map.has(day)) {
        map.set(day, []);
      }
      map.get(day).push(event);
    });

    this.groupedEvents = Array.from(map.keys())
      .sort((a, b) => a - b)
      .map(day => ({
        day: day,
        events: map.get(day)
      }));
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

  isCompletedEvent(tournamentEvent: TournamentEvent): boolean {
    return !!this.completionStatus[tournamentEvent.id];
  }

  isGiantRRDivisionCompleted(tournamentEvent: TournamentEvent, division: string): boolean {
    return this.isCompletedRRDivision(tournamentEvent, division);
  }

  onSelectGiantRREvent(tournamentEvent: TournamentEvent, division: string) {
    this.selectedEvent = tournamentEvent;
    this.prizeDataList = this.getRRDivisionPrizeData(tournamentEvent, division);
  }

  get giantRREventDivisions(): Map<number, string[]> {
    const map = new Map<number, string[]>();
    if (this.events) {
      this.events.forEach(event => {
        if (this.isGiantRREvent(event)) {
          map.set(event.id, this.getRRDivisions(event));
        }
      });
    }
    return map;
  }

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

  private prepareOneEventPrizeData(tournamentEvent: TournamentEvent) {
    const definedPrizeInfoList = tournamentEvent.configuration?.prizeInfoList;

    // Evaluate if configuration data is completely missing or empty array
    if (!definedPrizeInfoList || definedPrizeInfoList.length === 0) {
      this.isPrizeInfoMissing.set(tournamentEvent.id, true);
    } else {
      this.isPrizeInfoMissing.set(tournamentEvent.id, false);
    }
    let prizeInfoList = (definedPrizeInfoList && definedPrizeInfoList.length > 0) ? definedPrizeInfoList : this.prepareDefaultPrizeInfos('A');

    const finalPlayerRankings = tournamentEvent.configuration?.finalPlayerRankings ?? {};
    const prizeDataList = [];
    prizeInfoList.forEach((prizeInfo: PrizeInfo) => {
      const placeEnd = (prizeInfo.awardedForPlaceRangeEnd === 0)
        ? prizeInfo.awardedForPlace : prizeInfo.awardedForPlaceRangeEnd;
      for (let place = prizeInfo.awardedForPlace; place <= placeEnd; place++) {
        const playerFullName = finalPlayerRankings[place];
        const prizeData: PrizeData = {
          division: prizeInfo.division || '',
          place: place,
          prizeMoneyAmount: prizeInfo.prizeMoneyAmount || 0,
          awardTrophy: prizeInfo.awardTrophy || false,
          awardType: prizeInfo.awardType || 'Trophy',
          playerFullName: playerFullName
        };
        prizeDataList.push(prizeData);
      }
    });

    return prizeDataList.sort((a, b) => a.place - b.place);
  }

  private prepareDefaultPrizeInfos(division: string) {
    const defaultPrizeInfoList = [];
    defaultPrizeInfoList.push({division: division, awardedForPlace: 1, awardedForPlaceRangeEnd: 0, prizeMoneyAmount: 0, awardTrophy: true});
    defaultPrizeInfoList.push({division: division, awardedForPlace: 2, awardedForPlaceRangeEnd: 0, prizeMoneyAmount: 0, awardTrophy: true});
    defaultPrizeInfoList.push({division: division, awardedForPlace: 3, awardedForPlaceRangeEnd: 4, prizeMoneyAmount: 0, awardTrophy: true});
    return defaultPrizeInfoList;
  }

  private determineEventCompletionStatus(eventPrizeData: PrizeData[]): boolean {
    let allPlayersDetermined = eventPrizeData?.length > 0;
    eventPrizeData.forEach((prizeData: PrizeData) => {
      const playerDetermined = (prizeData.playerFullName != null && prizeData.playerFullName !== '');
      allPlayersDetermined = allPlayersDetermined && playerDetermined;
    });
    return allPlayersDetermined;
  }

  private prepareRoundRobinPrizeData() {
    const divisionsPrizeDataList: Map<number, PrizeData []> = new Map<number, PrizeData []>();
    this.events.forEach((tournamentEvent: TournamentEvent) => {
      if (tournamentEvent.drawMethod === DrawMethod.DIVISION && tournamentEvent.playersToAdvance === 0) {
        const rrPrizeData: PrizeData [] = [];
        const prizeInfoList = tournamentEvent.configuration?.prizeInfoList ?? [];

        // Track configurations for giant round robin events globally
        if (!prizeInfoList || prizeInfoList.length === 0) {
          this.isPrizeInfoMissing.set(tournamentEvent.id, true);
        } else {
          this.isPrizeInfoMissing.set(tournamentEvent.id, false);
        }

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
                      awardType: prizeInfo.awardType,
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
    this.divisionsPrizeDataList.forEach((eventPrizeDataList: PrizeData[], eventId: number) => {
      if (tournamentEvent.id === eventId) {
        divisionPrizeData = eventPrizeDataList.filter((prizeData: PrizeData) => {
          return (prizeData.division === division);
        });
      }
    });
    return divisionPrizeData;
  }

  protected getAwardType(prizeData) {
    if (!prizeData.awardType) {
      return prizeData.awardTrophy ? 'T' : '';
    } else if (prizeData.awardTrophy) {
      if (prizeData.awardType === PrizeInfo.AWARD_TYPE_TROPHY){
        return 'T';
      } else if (prizeData.awardType === PrizeInfo.AWARD_TYPE_MEDAL) {
        return 'M';
      } else {
        let awardTypeAbbreviation = "";
        const parts = prizeData.awardType.split(" ");
        for (const part of parts) {
          awardTypeAbbreviation += part.substring(0, 1);
        }
        return awardTypeAbbreviation.toUpperCase();
      }
    }
    return '';
  }
}

export class PrizeData {
  division: string;
  place: number;
  prizeMoneyAmount: number;
  awardTrophy: boolean;
  awardType: string;
  playerFullName: string;
}
