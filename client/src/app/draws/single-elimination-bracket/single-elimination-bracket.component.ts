import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {NgttRound, NgttTournament} from 'ng-tournament-tree/lib/declarations/interfaces';
import {DrawRound} from '../model/draw-round.model';
import {DrawItem} from '../model/draw-item.model';
import {Match} from '../model/match.model';

@Component({
  selector: 'app-single-elimination-bracket',
  templateUrl: './single-elimination-bracket.component.html',
  styleUrls: ['./single-elimination-bracket.component.scss']
})
export class SingleEliminationBracketComponent implements OnInit, OnChanges {

  @Input()
  rounds: number [] = [];

  @Input()
  tournament: NgttTournament;

  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
  }
}
