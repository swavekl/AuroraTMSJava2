import {ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {MonitorMessage} from '../model/monitor-message.model';
import {Match} from '../../matches/model/match.model';
import {takeWhile, timer} from 'rxjs';
import {finalize, tap} from 'rxjs/operators';
import {OrderOfServingCalculator} from '../../shared/serve-order/order-of-serving-calculator';
import {CardsInfo} from '../../shared/cards-display/cards-info.model';
import {MonitorMessageType} from '../model/monitor-message-type';

@Component({
    selector: 'app-monitor-display',
    templateUrl: './monitor-display.component.html',
    styleUrls: ['./monitor-display.component.scss'],
    standalone: false
})
export class MonitorDisplayComponent implements OnInit, OnChanges {

  @Input()
  matchData: MonitorMessage;

  @Input()
  isConnected: boolean;

  // table number this screen is connected to
  @Input()
  tableNumber: number;

  // array so we can use iteration in the template
  games: number[];
  numGamesWonByA: number;
  numGamesWonByB: number;

  // current game score
  gameScoreSideA: number;
  gameScoreSideB: number;

  currentGameIndex: number;

  match: Match;

  timerValue: number = 0;
  timerRunning: boolean = false;

  timerLabel: string;

  playerACardsInfo: CardsInfo;
  playerBCardsInfo: CardsInfo;

  private orderOfServingCalculator: OrderOfServingCalculator;

  constructor(protected cdr: ChangeDetectorRef) {
    this.games = Array(5);
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const matchDataChange: SimpleChange = changes.matchData;
    if (matchDataChange != null) {
      const mm: MonitorMessage = matchDataChange.currentValue;
      if (mm != null) {
        // console.log('monitor display got changes', mm.numberOfGames);
        this.games = Array(mm.numberOfGames);

        this.match = mm.match;
        this.getScoreInGames(mm.numberOfGames, mm.pointsPerGame, this.match);
        this.currentGameIndex = this.getCurrentGameIndex(mm.numberOfGames, mm.pointsPerGame);
        this.getCurrentGameScore();
        this.getTimerData(mm.match.messageType);

        if (this.orderOfServingCalculator == null) {
          let orderOfServingCalculator = new OrderOfServingCalculator(
            mm.numberOfGames, mm.doubles, this.match.playerAProfileId, this.match.playerBProfileId);
          if (this.match.servingOrderStateJSON != null && this.match?.servingOrderStateJSON != '') {
            orderOfServingCalculator.fromJson(this.match.servingOrderStateJSON);
          }
          this.orderOfServingCalculator = orderOfServingCalculator;
        } else {
          if (this.match.servingOrderStateJSON != null && this.match?.servingOrderStateJSON != '') {
            this.orderOfServingCalculator.fromJson(this.match.servingOrderStateJSON);
          }
        }

        const infoA: CardsInfo = new CardsInfo();
        if (this.match.playerACardsJSON != null) {
          infoA.fromJson(this.match.playerACardsJSON);
        }
        this.playerACardsInfo = infoA;

        const infoB: CardsInfo = new CardsInfo();
        if (this.match.playerBCardsJSON != null) {
          infoB.fromJson(this.match.playerBCardsJSON);
        }
        this.playerBCardsInfo = infoB;
      }
    }
  }

  public getCurrentGameScore() {
    switch (this.currentGameIndex) {
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

  getScoreInGames(numberOfGames: number, pointsPerGame: number, match: Match) {
    let numGamesWonByA = 0;
    let numGamesWonByB = 0;
    for (let i = 0; i < numberOfGames; i++) {
      let playerAGameScore = 0;
      let playerBGameScore = 0;
      switch (i) {
        case 0:
          playerAGameScore = match.game1ScoreSideA;
          playerBGameScore = match.game1ScoreSideB;
          break;
        case 1:
          playerAGameScore = match.game2ScoreSideA;
          playerBGameScore = match.game2ScoreSideB;
          break;
        case 2:
          playerAGameScore = match.game3ScoreSideA;
          playerBGameScore = match.game3ScoreSideB;
          break;
        case 3:
          playerAGameScore = match.game4ScoreSideA;
          playerBGameScore = match.game4ScoreSideB;
          break;
        case 4:
          playerAGameScore = match.game5ScoreSideA;
          playerBGameScore = match.game5ScoreSideB;
          break;
        case 5:
          playerAGameScore = match.game6ScoreSideA;
          playerBGameScore = match.game6ScoreSideB;
          break;
        case 6:
          playerAGameScore = match.game7ScoreSideA;
          playerBGameScore = match.game7ScoreSideB;
          break;
      }

      if (Match.isGameWon(playerAGameScore, playerBGameScore, pointsPerGame)) {
        numGamesWonByA++;
      } else if (Match.isGameWon(playerBGameScore, playerAGameScore, pointsPerGame)) {
        numGamesWonByB++;
      }
    }

    this.numGamesWonByA = numGamesWonByA;
    this.numGamesWonByB = numGamesWonByB;
  }

  private getCurrentGameIndex(numberOfGames: number, pointsPerGame: number) {
    const totalGamesPlayed = this.numGamesWonByA + this.numGamesWonByB;
    const matchFinished = Match.isMatchFinished(this.match, numberOfGames, pointsPerGame);
    return matchFinished ? (totalGamesPlayed - 1) : Math.min(totalGamesPlayed, (numberOfGames - 1));
  }

  protected readonly isNaN = isNaN;

  private getTimerData(messageType: MonitorMessageType) {
    if (!this.timerRunning) {
      let duration: number = 0;
      let label = 'Break';
      switch (messageType) {
        case MonitorMessageType.WarmupStarted:
          label = 'Warmup';
          duration = 120;
          break;
        case MonitorMessageType.BreakStarted:
          label = 'Break';
          duration = 60;
          break;
        case MonitorMessageType.TimeoutStarted:
          label = 'Timeout';
          duration = 60;
          break;
      }
      if (duration > 0) {
        this.startTimer(duration, label);
      }
    } else {
      if (messageType === MonitorMessageType.WarmupStopped ||
        messageType === MonitorMessageType.BreakStopped ||
        messageType === MonitorMessageType.TimeoutStopped) {
        this.stopTimer();
      }
    }
  }

  startTimer(duration: number, label: string) {
    if (!this.timerRunning) {
      this.timerRunning = true;
      this.timerValue = duration;
      this.timerLabel = label;
      timer(1000, 1000)
        .pipe(
          takeWhile(() => this.timerValue > 0),
          tap(() => this.timerValue--),
          finalize(() => {
            this.timerRunning = false;
            this.timerValue = 0;
            this.timerLabel = null;
          })
        ).subscribe();
    }
  }

  stopTimer() {
    if (this.timerRunning) {
      this.timerValue = 0;
      this.timerRunning = false;
    }
  }

  serverOrReceiver(playerName: string) {
    let playerProfileId = null;
    if (this.matchData.doubles) {
      const teamAProfileIds = this.match.playerAProfileId.split(';');
      const teamBProfileIds = this.match.playerBProfileId.split(';');
      if (playerName === this.matchData.playerAName) {
        playerProfileId = teamAProfileIds[0];
      } else if (playerName === this.matchData.playerAPartnerName) {
        playerProfileId = teamAProfileIds[1];
      } else if (playerName === this.matchData.playerBName) {
        playerProfileId = teamBProfileIds[0];
      } else if (playerName === this.matchData.playerBPartnerName) {
        playerProfileId = teamBProfileIds[1];
      }
    } else {
      if (playerName === this.matchData.playerAName) {
        playerProfileId = this.match.playerAProfileId;
      } else {
        playerProfileId = this.match.playerBProfileId;
      }
    }

    if (playerProfileId) {
      if (this.orderOfServingCalculator.isPlayerServer(playerProfileId)) {
        return 'S';
      } else if (this.orderOfServingCalculator.isPlayerReceiver(playerProfileId)) {
        return this.matchData.doubles ? 'R' : '';
      }
    }
    // neither
    return '';
  }

  getCardsInfo(playerACardsJSON: string) {

    return undefined;
  }
}
