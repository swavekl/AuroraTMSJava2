import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {MatchCard} from '../model/match-card.model';
import {Match} from '../model/match.model';

@Component({
  selector: 'app-player-matches',
  templateUrl: './player-matches.component.html',
  styleUrls: ['./player-matches.component.scss']
})
export class PlayerMatchesComponent implements OnInit, OnChanges {

  @Input()
  public matchCard: MatchCard;

  @Input()
  public pointsPerGame: number;

  private expandedMatchIndex: number;

  // array so we can use iteration in the template
  games: number [];

  constructor() {
    this.expandedMatchIndex = 0;
    this.games = [];
    this.pointsPerGame = 11;
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const matchCardChanges: SimpleChange = changes.matchCard;
    if (matchCardChanges) {
      const matchCard = matchCardChanges.currentValue;
      if (matchCard) {
        const numGames = matchCard.numberOfGames === 0 ? 5 : matchCard.numberOfGames;
        this.games = Array(numGames);
      }
    }
  }

  isMatchExpanded(index: number): boolean {
    return this.expandedMatchIndex === index;
  }

  expandMatch(index: number) {
    this.expandedMatchIndex = index;
  }

  isMatchWinner(match: Match, profileId: string): boolean {
    return (match) ? Match.isMatchWinner(profileId, match, this.matchCard.numberOfGames, this.pointsPerGame) : false;
  }

}
