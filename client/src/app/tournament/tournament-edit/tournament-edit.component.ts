import {Component, Input, OnInit} from '@angular/core';
import {Tournament} from '../tournament.model';

@Component({
  selector: 'app-tournament-edit',
  templateUrl: './tournament-edit.component.html',
  styleUrls: ['./tournament-edit.component.css']
})
export class TournamentEditComponent implements OnInit {

  @Input()
  tournament: Tournament;

  constructor() {
  }

  ngOnInit() {
  }

  onEnter() {
    console.log('entering the tournament');
  }
}
