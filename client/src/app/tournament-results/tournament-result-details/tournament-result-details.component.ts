import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {EventResults} from '../model/event-results';
import {DrawItem} from '../../draws/model/draw-item.model';
import {DrawType} from '../../draws/model/draw-type.enum';
import {DrawRound} from '../../draws/model/draw-round.model';
import {Match} from '../../draws/model/match.model';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {PlayerResults} from '../model/player-results';
import {NgttRound, NgttTournament} from 'ng-tournament-tree/lib/declarations/interfaces';
import {MatchResult} from '../model/match-result';

@Component({
  selector: 'app-tournament-result-details',
  templateUrl: './tournament-result-details.component.html',
  styleUrls: ['./tournament-result-details.component.scss']
})
export class TournamentResultDetailsComponent implements OnInit, OnChanges {

  @Input()
  eventResultsList: EventResults[];

  @Input()
  event: TournamentEvent;

  singleEliminationRounds: DrawRound[] = [];

  tournament: NgttTournament;

  roundNumbers: number[] = [];

  constructor() {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.eventResultsList != null && this.event != null) {
      const tempSeDrawItems: DrawItem [] = this.makeTempDrawItems();
      this.singleEliminationRounds = this.makeDrawRounds(tempSeDrawItems);
      this.makeNgttTournament();
    }
  }

  private makeTempDrawItems() {
    const tempSeDrawItems: DrawItem[] = [];
    for (const eventResults of this.eventResultsList) {
      if (eventResults.singleElimination) {
        const playerResultsList: PlayerResults[] = eventResults.playerResultsList;
        for (const playerResult of playerResultsList) {
          const drawItem: DrawItem = {
            id: 1,
            drawType: DrawType.SINGLE_ELIMINATION,
            clubName: null,
            eventFk: this.event.id,
            byeNum: playerResult.byeNumber,
            conflicts: null,
            groupNum: eventResults.groupNumber,
            placeInGroup: 1, // todo
            playerId: playerResult.profileId,
            playerName: playerResult.fullName,
            round: eventResults.round,
            rating: playerResult.rating,
            seSeedNumber: playerResult.seSeedNumber,
            state: null // todo
          };
          tempSeDrawItems.push(drawItem);
        }
      }
    }
    return tempSeDrawItems;
  }

  private makeDrawRounds(drawItems: DrawItem[]): DrawRound[] {
    const drawRounds: DrawRound[] = [];
    for (const drawItem of drawItems) {
      this.addDrawItemToRound(drawRounds, drawItem);
    }
    drawRounds.sort((dr1: DrawRound, dr2: DrawRound): number => {
      return (dr1.round < dr2.round) ? 1 : -1;
    });
    return drawRounds;
  }

  private addDrawItemToRound(drawRounds: DrawRound[], drawItem: DrawItem) {
    let foundDrawRound = null;
    for (const drawRound of drawRounds) {
      if (drawRound.round === drawItem.round) {
        foundDrawRound = drawRound;
        break;
      }
    }
    if (!foundDrawRound) {
      foundDrawRound = new DrawRound();
      foundDrawRound.round = drawItem.round;
      drawRounds.push(foundDrawRound);
    }
    const roundDrawItems: DrawItem[] = foundDrawRound.drawItems;
    roundDrawItems.push(drawItem);
  }

  private makeNgttTournament() {
    const rounds: NgttRound [] = [];
    const drawRounds: DrawRound[] = this.singleEliminationRounds;
    const roundNumbers: number [] = [];
    for (let i = 0; i < drawRounds.length; i++) {
      const drawRound = drawRounds[i];
      roundNumbers.push(drawRound.round);
      const drawItems: DrawItem [] = drawRound.drawItems;
      const roundMatches = [];
      for (let j = 0; j < drawItems.length;) {
        const drawItemLeft: DrawItem = drawItems[j];
        const drawItemRight: DrawItem = drawItems[j + 1];
        const match: Match = new Match();
        match.opponentA = drawItemLeft;
        match.opponentB = drawItemRight;
        match.time = 0;
        match.tableNum = 0;  // for now
        this.findMatchResultAndWinner(drawItemLeft.playerId, drawItemRight.playerId, drawRound.round, match);
        match.showSeedNumber = (i === 0); // show seed number for first round only

        roundMatches.push(match);
        j += 2;
      }
      const type = ((i + 1) === drawRounds.length) ? 'Final' : 'Winnerbracket';
      const round: NgttRound = {type: type, matches: roundMatches};
      rounds.push(round);
    }
    if (rounds.length > 0) {
      this.tournament = {
        rounds: rounds
      };
    }
    this.roundNumbers = roundNumbers;
  }

  private findMatchResultAndWinner(playerIdA: string, playerIdB: string, round: number, match: Match) {
    let compactMatchResultArray = [];
    let resultFound = false;
    for (const eventResults of this.eventResultsList) {
      if (eventResults.singleElimination && eventResults.round === round) {
        const playerResultsList: PlayerResults[] = eventResults.playerResultsList;
        const playerAResult: PlayerResults = this.findPlayerResults(playerIdA, playerResultsList);
        const playerBResult: PlayerResults = this.findPlayerResults(playerIdB, playerResultsList);
        if (playerAResult != null && playerBResult != null) {
          let matchResults: MatchResult [] = null;
          if (playerAResult.rank === 1) {
            // player A won
            matchResults = playerAResult.matchResults;
            match.opponentAWon = true;
          } else if (playerBResult.rank === 1) {
            // player B won
            matchResults = playerBResult.matchResults;
            match.opponentAWon = false;
          }
          if (matchResults) {
            for (const matchResult of matchResults) {
              // not A vs A match
              if (matchResult.playerALetter !== matchResult.playerBLetter) {
                const strCompactMatchRestult: string = matchResult.compactMatchResult;
                if (strCompactMatchRestult) {
                  compactMatchResultArray = strCompactMatchRestult.split(',');
                  resultFound = true;
                }
              }
            }
          }
        }
      }
      if (resultFound) {
        break;
      }
    }
    match.result = compactMatchResultArray;
    return compactMatchResultArray;
  }

  private findPlayerResults(playerId: string, playerResultsList: PlayerResults[]): PlayerResults {
    for (const playerResult of playerResultsList) {
      if (playerResult.profileId === playerId) {
        return playerResult;
      }
    }
    return null;
  }

  public getBracketsHeight(): string {
    return (window.innerHeight - 212) + 'px';
  }

  getPlayerNames(playerName: string) {
    if (!this.event.doubles) {
      return playerName;
    } else {
      const split = playerName.replace('/ ', '</br>');
      console.log('split', split);
      return split;
    }
  }

}
