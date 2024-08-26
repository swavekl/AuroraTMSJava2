import {Component, EventEmitter, Input, OnChanges, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {Match} from '../../matches/model/match.model';
import {OrderOfServingCalculator} from '../../shared/serve-order/order-of-serving-calculator';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {TimerPopupComponent} from '../timer-popup/timer-popup.component';
import {CardsInfo} from '../../shared/cards-display/cards-info.model';
import {CardsPopupComponent} from '../cards-popup/cards-popup.component';

@Component({
  selector: 'app-score-board-match-start',
  templateUrl: './score-board-match-start.component.html',
  styleUrl: './score-board-match-start.component.scss'
})
export class ScoreBoardMatchStartComponent implements OnChanges {

  @Input()
  match: Match;

  @Input()
  public playerAName: string;

  @Input()
  public playerBName: string;

  @Input()
  public pointsPerGame: number;

  @Input()
  public numberOfGames: number;

  @Input()
  public doubles: boolean;

  @Output()
  timerEvent: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  serverReceiverSaveEvent: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  matchEvent: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  public saveMatch: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  public cancelMatch: EventEmitter<any> = new EventEmitter<any>();

  private orderOfServingCalculator: OrderOfServingCalculator;

  private playerProfileIdToNameMap: any = {};

  public playerACardsInfo: CardsInfo;

  public playerBCardsInfo: CardsInfo;

  // used to indicate the serving side to start serving in the match
  servingSide: string = 'left';

  // index of the game to show 0, 1, etc
  public gameToShowIndex: number;

  // array so we can use iteration in the template
  games: number [];

  // game scores that are edited
  gameScoreSideA: number;
  gameScoreSideB: number;

  dirty: boolean;

  timerRunning: boolean = false;

  // timeout started for player A or B
  timeoutForPlayer: string = null;

  constructor(private dialog: MatDialog) {
    this.games = [];
    this.pointsPerGame = 11;
    this.gameToShowIndex = 0;
    this.numberOfGames = 5;
    this.gameScoreSideA = 0;
    this.gameScoreSideB = 0;
    this.dirty = false;
  }

  ngOnChanges(changes: SimpleChanges): void {
    const numberOfGamesChanges: SimpleChange = changes.numberOfGames;
    if (numberOfGamesChanges) {
      const numberOfGames = numberOfGamesChanges.currentValue;
      if (numberOfGames) {
        this.games = Array(numberOfGames);
      }
    }

    const matchChange: SimpleChange = changes.match;
    if (matchChange) {
      if (matchChange.currentValue != null) {
        this.match = matchChange.currentValue;
        if (this.doubles != null && this.numberOfGames != 0) {
          if (this.orderOfServingCalculator == null) {
            this.orderOfServingCalculator = new OrderOfServingCalculator(this.numberOfGames, this.doubles,
              this.match.playerAProfileId, this.match.playerBProfileId);
            if (this.match.servingOrderStateJSON != null && this.match.servingOrderStateJSON != '') {
              this.orderOfServingCalculator.fromJson(this.match.servingOrderStateJSON);
            }
          }
          this.gameToShowIndex = this.orderOfServingCalculator.currentGame;
        }

        this.servingSide = this.match.initialServerSide ?? 'left';

        // get the score from the current game
        this.getGameScore();

        if (this.playerAName != null && this.playerBName != null) {
          this.playerProfileIdToNameMap = this.makeProfileIdMap();
        }

        this.playerACardsInfo = new CardsInfo();
        if (this.match.playerACardsJSON != null) {
          this.playerACardsInfo.fromJson(this.match.playerACardsJSON);
        }
        this.match.playerACardsJSON = this.playerACardsInfo.toJson();

        this.playerBCardsInfo = new CardsInfo();
        if (this.match.playerBCardsJSON != null) {
          this.playerBCardsInfo.fromJson(this.match.playerBCardsJSON);
        }
        this.match.playerBCardsJSON = this.playerBCardsInfo.toJson();
      }
    }
  }

  private makeProfileIdMap() {
    let profileIdToNameMap = {};
    if (this.doubles) {
      const aProfileIds = this.match.playerAProfileId.split(';');
      const aNames = this.playerAName.split(' / ');
      profileIdToNameMap[aProfileIds[0]] = aNames[0];
      profileIdToNameMap[aProfileIds[1]] = aNames[1];
      const bProfileIds = this.match.playerBProfileId.split(';');
      const bNames = this.playerBName.split(' / ');
      profileIdToNameMap[bProfileIds[0]] = bNames[0];
      profileIdToNameMap[bProfileIds[1]] = bNames[1];
    } else {
      profileIdToNameMap[this.match.playerAProfileId] = this.playerAName;
      profileIdToNameMap[this.match.playerBProfileId] = this.playerBName;
    }
    return profileIdToNameMap;
  }


  isServerAndReceiverRecorded(): boolean {
    return this.match?.initialServerSide != null && this.match?.initialServerSide != '';
  }

  recordServerAndReceiver() {
    const json = this.orderOfServingCalculator.toJson();
    const updatedMatch: Match = {
      ...this.match,
      servingOrderStateJSON: json,
      initialServerSide: this.servingSide
    };
    this.serverReceiverSaveEvent.emit({updatedMatch: updatedMatch});
  }

  startWarmup() {
    if (this.isServerAndReceiverRecorded()) {
      this.match = {
        ...this.match,
        warmupStarted: true
      };
      this.timerEvent.emit({action: 'startWarmup', updatedMatch: this.match});

      this.showTimerPopup(120, 'Warmup', 'stopWarmup');
    } else {
      const config = {
        width: '450px', height: '230px', data: {
          message: 'Sides of players and initial server have not been confirmed.  You can\'t change them after the Warmup is started',
          showOk: true, showCancel: false, okText: 'Close'
        }
      };
      const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
      dialogRef.afterClosed().subscribe(result => {
      });

    }
  }

  showTimerPopup(duration: number, title: string, eventName: string) {
    const config = {
      width: '500px', height: '200px', data: {
        duration: duration, title: title, eventName: eventName
      }
    };
    const dialogRef = this.dialog.open(TimerPopupComponent, config);
    dialogRef.afterClosed().subscribe(resultEventName => {
      if (resultEventName === 'stopWarmup') {
        this.stopWarmup();
      } else if (resultEventName === 'stopTimeout') {
        this.stopTimeout();
      }
    });

  }

  stopWarmup() {
    this.match = {
      ...this.match,
      warmupStarted: false
    };
    this.timerEvent.emit({action: 'stopWarmup', updatedMatch: this.match});
  }

  canStartWarmup() {
    return this.isMatchNotStarted() && this.isServerAndReceiverRecorded();
  }

  stopTimeout() {
    this.timerEvent.emit({action: 'stopTimeout', updatedMatch: this.match});
  }


  isMatchNotStarted() {
    if (this.match != null) {
      return (this.match.game1ScoreSideA === 0 && this.match.game1ScoreSideB === 0) &&
        (this.match.game2ScoreSideA === 0 && this.match.game2ScoreSideB === 0) &&
        (this.match.game3ScoreSideA === 0 && this.match.game3ScoreSideB === 0) &&
        (this.match.game4ScoreSideA === 0 && this.match.game4ScoreSideB === 0) &&
        (this.match.game5ScoreSideA === 0 && this.match.game5ScoreSideB === 0) &&
        (this.match.game6ScoreSideA === 0 && this.match.game6ScoreSideB === 0) &&
        (this.match.game7ScoreSideA === 0 && this.match.game7ScoreSideB === 0);
    } else {
      return false;
    }
  }

  back() {
    this.matchEvent.emit({action: 'back'});
  }

  swapSides() {
    this.orderOfServingCalculator.switchSides();
  }

  startMatch() {
    this.matchEvent.emit({action: 'startMatch'});
  }

  getPlayerName(leftSide: boolean, index: number): string {
    const playerProfileId = this.getPlayerProfileId(leftSide, index);
    return this.playerProfileIdToNameMap[playerProfileId];
  }

  switchPlayers(leftSide: boolean) {
    this.orderOfServingCalculator.switchPlayers(leftSide);
    const side = (this.servingSide === 'left');
    this.onSelectServer(side);
  }

  getPlayerProfileId(leftSide: boolean, index: number) {
    const side = (leftSide) ? 'L' : 'R';
    const sideAndNumber = `${side}${index + 1}`;
    return this.orderOfServingCalculator.lookupPlayerProfile(sideAndNumber);
  }

  getPlayerLetter(leftSide: boolean, index: number) {
    const side = (leftSide) ? 'L' : 'R';
    const sideAndNumber = `${side}${index + 1}`;
    return this.orderOfServingCalculator.lookupPlayerInPosition(sideAndNumber);
  }

  isServer(leftSide: boolean) {
    const isServer = this.orderOfServingCalculator.isServer(leftSide);
    // console.log('is ' + (leftSide ? 'L' : 'R') + ' side a server -> ' + isServer);
    return isServer;
  }

  onSelectServer(leftSide: boolean) {
    const serverSideAndNumber = (leftSide) ? 'L2' : 'R1';
    const receiverSideAndNumber = (leftSide) ? 'R1' : 'L2';
    const server = this.orderOfServingCalculator.lookupPlayerInPosition(serverSideAndNumber);
    const receiver = this.orderOfServingCalculator.lookupPlayerInPosition(receiverSideAndNumber);
    this.orderOfServingCalculator.recordServerAndReceiver(server, receiver);
  }


  // previousGame() {
  //   this.saveGameScore();
  //   this.determineServerAndReceiver();
  //   if (this.dirty) {
  //     this.saveMatch.emit({updatedMatch: this.match, backToMatchCard: false});
  //     this.dirty = false;
  //   }
  //   this.gameToShowIndex--;
  //   this.getGameScore();
  // }
  //
  // hasPreviousGame() {
  //   return this.gameToShowIndex > 0;
  // }

  nextGame() {
    this.saveGameScore();
    this.advanceToNextGame();
    // if (this.dirty) {
    this.saveMatch.emit({updatedMatch: this.match, backToMatchCard: false});
    // this.dirty = false;
    // }
    this.gameToShowIndex++;
    this.getGameScore();
  }

  hasNextGame() {
    let hasNextGame: boolean;
    const matchFinished = Match.isMatchFinished(this.match, this.numberOfGames, this.pointsPerGame);
    if (matchFinished) {
      // console.log('matchFinished', matchFinished);
      // don't allow to advance past the last entered match
      // they need to change prior games to do that
      const nextBlankGameIndex = Match.nextNotEnteredGameIndex(this.match, this.numberOfGames);
      hasNextGame = this.gameToShowIndex < (nextBlankGameIndex - 1);
    } else {
      // let them enter games until match is fully entered
      hasNextGame = this.gameToShowIndex < (this.numberOfGames - 1);
    }
    // console.log('in hasNextGame', hasNextGame);
    return hasNextGame;
  }

  private isWinnerSelected(): boolean {
    return !(this.gameScoreSideB === 0 && this.gameScoreSideA === 0);
  }

  // isSubtractDisabled() {
  //   return !this.isWinnerSelected();
  // }

  getScore(leftSide: boolean) {
    const playerIndex = this.getPlayerIndex(leftSide);
    return (playerIndex === 0) ? this.gameScoreSideA : this.gameScoreSideB;
  }

  subtractPoint(leftSide: boolean) {
    const playerIndex = this.getPlayerIndex(leftSide);
    this.subtractPointInternal(playerIndex);
  }

  subtractPointInternal(playerIndex: number) {
    if (playerIndex === 0) {
      this.gameScoreSideA -= (this.gameScoreSideA > 0) ? 1 : 0;
    } else {
      this.gameScoreSideB -= (this.gameScoreSideB > 0) ? 1 : 0;
    }
    this.saveGameScore();
    this.determineServerAndReceiver();
    this.saveMatch.emit({updatedMatch: this.match, backToMatchCard: false});
    this.dirty = true;
  }

  addPoint(leftSide: boolean) {
    const playerIndex = this.getPlayerIndex(leftSide);
    this.addPointInternal(playerIndex);
  }

  addPointInternal(playerIndex: number) {
    if (playerIndex === 0) {
      this.gameScoreSideA++;
    } else {
      this.gameScoreSideB++;
    }
    this.saveGameScore();
    this.determineServerAndReceiver();
    this.saveMatch.emit({updatedMatch: this.match, backToMatchCard: false});
    this.dirty = true;
  }

  private getPlayerIndex(leftSide: boolean) {
    const playerSideAndPosition = (leftSide) ? 'L2' : 'R1';
    const playerLetter = this.orderOfServingCalculator.lookupPlayerInPosition(playerSideAndPosition);
    // console.log('playerSideAndPosition ' + playerSideAndPosition + ' playerLetter ' + playerLetter);
    return (playerLetter === 'A' || playerLetter === 'B') ? 0 : 1;
  }

  private getNextGameIndex(match: Match): number {
    const matchFinished = Match.isMatchFinished(match, this.numberOfGames, this.pointsPerGame);
    // console.log('matchFinished', matchFinished);
    // const nextNotEnteredIndex = Match.nextNotEnteredGameIndex(match, this.numberOfGames);
    if (matchFinished) {
      // show first game - maybe they want to correct it
      return 0;
    } else {
      return Match.nextNotEnteredGameIndex(match, this.numberOfGames);
    }
  }

  /**
   * Sets current game score
   */
  getGameScore() {
    switch (this.gameToShowIndex) {
      case 0:
        this.gameScoreSideA = this.match.game1ScoreSideA;
        this.gameScoreSideB = this.match.game1ScoreSideB;
        break;
      case 1:
        this.gameScoreSideA = this.match.game2ScoreSideA;
        this.gameScoreSideB = this.match.game2ScoreSideB;
        break;
      case 2:
        this.gameScoreSideA = this.match.game3ScoreSideA;
        this.gameScoreSideB = this.match.game3ScoreSideB;
        break;
      case 3:
        this.gameScoreSideA = this.match.game4ScoreSideA;
        this.gameScoreSideB = this.match.game4ScoreSideB;
        break;
      case 4:
        this.gameScoreSideA = this.match.game5ScoreSideA;
        this.gameScoreSideB = this.match.game5ScoreSideB;
        break;
      case 5:
        this.gameScoreSideA = this.match.game6ScoreSideA;
        this.gameScoreSideB = this.match.game6ScoreSideB;
        break;
      case 6:
        this.gameScoreSideA = this.match.game7ScoreSideA;
        this.gameScoreSideB = this.match.game7ScoreSideB;
        break;
      default:
        this.gameScoreSideA = 0;
        this.gameScoreSideB = 0;
        break;
    }
  }

  /**
   * Saves currently displayed game score
   */
  saveGameScore() {
    switch (this.gameToShowIndex) {
      case 0:
        this.match = {
          ...this.match,
          game1ScoreSideA: this.gameScoreSideA,
          game1ScoreSideB: this.gameScoreSideB
        };
        break;
      case 1:
        this.match = {
          ...this.match,
          game2ScoreSideA: this.gameScoreSideA,
          game2ScoreSideB: this.gameScoreSideB
        };
        break;
      case 2:
        this.match = {
          ...this.match,
          game3ScoreSideA: this.gameScoreSideA,
          game3ScoreSideB: this.gameScoreSideB
        };
        break;
      case 3:
        this.match = {
          ...this.match,
          game4ScoreSideA: this.gameScoreSideA,
          game4ScoreSideB: this.gameScoreSideB
        };
        break;
      case 4:
        this.match = {
          ...this.match,
          game5ScoreSideA: this.gameScoreSideA,
          game5ScoreSideB: this.gameScoreSideB
        };
        break;
      case 5:
        this.match = {
          ...this.match,
          game6ScoreSideA: this.gameScoreSideA,
          game6ScoreSideB: this.gameScoreSideB
        };
        break;
      case 6:
        this.match = {
          ...this.match,
          game7ScoreSideA: this.gameScoreSideA,
          game7ScoreSideB: this.gameScoreSideB
        };
        break;
    }
  }

  save() {
    if (this.isScoreValid()) {
      this.saveGameScore();
      this.saveMatch.emit({updatedMatch: this.match, backToMatchCard: true});
      this.dirty = false;
    }
  }

  isScoreValid(): boolean {
    if (this.gameScoreSideA > this.gameScoreSideB) {
      return (this.gameScoreSideA === this.pointsPerGame)
        ? ((this.gameScoreSideA - this.gameScoreSideB) >= 2)
        : ((this.gameScoreSideA - this.gameScoreSideB) === 2);
    } else if (this.gameScoreSideB > this.gameScoreSideA) {
      return (this.gameScoreSideB === this.pointsPerGame)
        ? ((this.gameScoreSideB - this.gameScoreSideA) >= 2)
        : ((this.gameScoreSideB - this.gameScoreSideA) === 2);
    } else {
      return false;
    }
  }

  startTimeout(playerLetter: string) {
    if (!this.timerRunning) {
      const sideATimeoutTaken = (playerLetter === 'A') || this.match.sideATimeoutTaken;
      const sideBTimeoutTaken = (playerLetter === 'B') || this.match.sideBTimeoutTaken;
      this.timeoutForPlayer = playerLetter;
      this.match = {
        ...this.match,
        sideATimeoutTaken: sideATimeoutTaken,
        sideBTimeoutTaken: sideBTimeoutTaken
      };
      this.timerEvent.emit({action: 'startTimeout', timeoutForPlayer: this.timeoutForPlayer, updatedMatch: this.match});
      this.showTimerPopup(60, 'Timeout', 'stopTimeout');
    }
  }

  canStartTimeout(playerLetter: string) {
    if (this.match != null && !this.timerRunning) {
      const matchStarted = !this.isMatchNotStarted();
      if (playerLetter === 'A') {
        return matchStarted && this.match.sideATimeoutTaken === false;
      } else if (playerLetter === 'B') {
        return matchStarted && this.match.sideBTimeoutTaken === false;
      }
    } else {
      return false;
    }
  }

  private determineServerAndReceiver() {
    if (this.orderOfServingCalculator != null) {
      this.orderOfServingCalculator.determineNextServerAndReceiver(this.gameScoreSideA, this.gameScoreSideB);
      const orderOfServingJSON = this.orderOfServingCalculator.toJson();
      this.match = {
        ...this.match,
        servingOrderStateJSON: orderOfServingJSON
      };
    }
  }

  private advanceToNextGame() {
    this.orderOfServingCalculator.startNextGame();
    this.orderOfServingCalculator.switchSides();
    // get side which is supposed to serve
    const isServingFromLeft = (this.match.initialServerSide === 'left');

    // get current player from the serving side
    this.orderOfServingCalculator.determineDefaultServerAndReceiver(isServingFromLeft);
  }

  endMatch() {
    // todo - default, scratches injury etc.
  }

  isGameFinished() {
    return Match.isGameFinished(this.gameScoreSideA, this.gameScoreSideB, this.pointsPerGame);
  }

  issueCard(leftSide: boolean) {
    const cardsInfo = this.getCardsInfo(leftSide);
    const playerIndex = this.getPlayerIndex(leftSide);
    const config = {
      width: "550px", height: "250px", data: {
        cardsInfo: cardsInfo
      }
    }
    const dialogRef = this.dialog.open(CardsPopupComponent, config);
    dialogRef.afterClosed().subscribe(updatedCardsInfo => {
      if (updatedCardsInfo != null) {
        const updatePlayerCardsInfoJSON = JSON.stringify(updatedCardsInfo);
        if (playerIndex === 0) {
          this.playerACardsInfo = updatedCardsInfo;
          this.match = {...this.match, playerACardsJSON: updatePlayerCardsInfoJSON };
        } else {
          this.playerBCardsInfo = updatedCardsInfo;
          this.match = {...this.match, playerBCardsJSON: updatePlayerCardsInfoJSON };
        }
        this.saveMatch.emit({updatedMatch: this.match, backToMatchCard: false});
        this.dirty = false;
      }
    });
  }

  getCardsInfo(leftSide: boolean) {
    const playerIndex = this.getPlayerIndex(leftSide);
    if (playerIndex === 0) {
      return this.playerACardsInfo;
    } else {
      return this.playerBCardsInfo;
    }
  }
}
