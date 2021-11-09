import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {MatchCard} from '../model/match-card.model';
import {Match} from '../model/match.model';
import {AuthenticationService} from '../../user/authentication.service';

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

  @Input()
  private thisPlayerProfileId: string;

  @Input()
  public doubles: boolean;

  private expandedMatchIndex: number;

  @Output()
  public back: EventEmitter<any> = new EventEmitter<any>();

  // array so we can use iteration in the template
  games: number [];

  constructor(private authenticationService: AuthenticationService) {
    this.expandedMatchIndex = 0;
    this.games = [];
    this.pointsPerGame = 11;
    this.thisPlayerProfileId = this.authenticationService.getCurrentUserProfileId();
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

  isPlayerMatch(match: Match): boolean {
    return (match.playerAProfileId.indexOf(this.thisPlayerProfileId) >= 0) ||
           (match.playerBProfileId.indexOf(this.thisPlayerProfileId) >= 0);
  }

  isNameBolded(profileIds: string): boolean {
    // console.log('profileIds: ' + profileIds + ' this.thisPlayerProfileId: ' + this.thisPlayerProfileId);
    return profileIds.indexOf(this.thisPlayerProfileId) >= 0;
  }

  isDoublesPlayerNameBolded(profileIds: string, index: number): boolean {
    // console.log('profileIds: ' + profileIds + ' this.thisPlayerProfileId: ' + this.thisPlayerProfileId);
    const profileIdsArray = profileIds.split(';');
    const doublesPlayerId = profileIdsArray[index];
    return doublesPlayerId === this.thisPlayerProfileId;
  }

  getDoublesPlayerName (profileIds: string, index: number): string {
    const playerNames: string = this.matchCard.profileIdToNameMap[profileIds];
    const playerNamesArray = playerNames.split('/');
    return playerNamesArray[index].trim();
  }

  getPlayerName(profileId: string): string {
    return this.matchCard.profileIdToNameMap[profileId];
  }

  onBack() {
    this.back.emit(null);
  }
}
