import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TournamentEventRound} from '../model/tournament-event-round.model';
import {DrawMethod} from '../model/draw-method.enum';
import {CommonRegexPatterns} from '../../../shared/common-regex-patterns';
import {TournamentEvent} from '../tournament-event.model';

@Component({
  selector: 'app-rounds-draws-config',
  standalone: false,
  templateUrl: './rounds-draws-config.component.html',
  styleUrl: './rounds-draws-config.component.scss'
})
export class RoundsDrawsConfigComponent {
  @Input() rounds!: TournamentEventRound[];

  @Input()
  days: any [] = [];

  @Input()
  startTimes: any [] = [];

  @Output()
  roundsChanged: EventEmitter<TournamentEventRound[]> = new EventEmitter();

  drawMethods: any [] = [
    {value: DrawMethod.SNAKE.valueOf(), label: 'Snake'},
    {value: DrawMethod.DIVISION.valueOf(), label: 'Division'},
    {value: DrawMethod.BY_RECORD.valueOf(), label: 'By Record'},
    {value: DrawMethod.SINGLE_ELIMINATION.valueOf(), label: 'Single Elimination'},
  ];

  // choices of number of games in a match
  numberOfGamesChoices: any [] = [
    {value: 3, label: 'Best of 3'},
    {value: 5, label: 'Best of 5'},
    {value: 7, label: 'Best of 7'}
  ];
  readonly NUMERIC_WITH_ZERO_REGEX = CommonRegexPatterns.NUMERIC_WITH_ZERO_REGEX;
  readonly NUMERIC_REGEX = CommonRegexPatterns.NUMERIC_REGEX;
  readonly TWO_DIGIT_NUMERIC_REGEX = CommonRegexPatterns.TWO_DIGIT_NUMERIC_REGEX;

  protected addRound() {
    const newRound = TournamentEvent.makeRound('Round X', null, DrawMethod.SNAKE,);
    this.rounds = [...this.rounds, newRound];
    this.roundsChanged.emit(this.rounds);
  }

  protected readonly DrawMethod = DrawMethod;
}
