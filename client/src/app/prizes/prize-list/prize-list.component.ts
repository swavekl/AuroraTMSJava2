import {Component, Input, OnInit} from '@angular/core';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {PrizeInfo} from '../../tournament/tournament-config/model/prize-info.model';

@Component({
  selector: 'app-prize-list',
  templateUrl: './prize-list.component.html',
  styleUrls: ['./prize-list.component.scss']
})
export class PrizeListComponent implements OnInit {

  @Input()
  events: TournamentEvent[];

  selectedEvent: TournamentEvent;

  prizeDataList: PrizeData [] = [];

  tournamentCurrency: 'USD';

  constructor() { }

  ngOnInit(): void {
  }

  onSelectEvent(tournamentEvent: TournamentEvent) {
    this.selectedEvent = tournamentEvent;
    this.preparePrize();
  }

  isSelected(tournamentEvent: TournamentEvent): boolean {
    return tournamentEvent.id === this.selectedEvent?.id;
  }

  private preparePrize() {
    if (this.selectedEvent) {
      const prizeInfoList = this.selectedEvent.configuration?.prizeInfoList ?? [];
      const finalPlayerRankings = this.selectedEvent.configuration?.finalPlayerRankings ?? {};
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
      this.prizeDataList = prizeDataList;
    }
  }
}

export class PrizeData {
  place: number;
  prizeMoneyAmount: number;
  awardTrophy: boolean;
  playerFullName: string;
}
