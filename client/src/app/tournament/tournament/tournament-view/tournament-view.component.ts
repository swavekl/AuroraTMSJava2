import {Component, Input, OnInit} from '@angular/core';
import {TournamentInfo} from '../tournament-info.model';

@Component({
  selector: 'app-tournament-view',
  templateUrl: './tournament-view.component.html',
  styleUrls: ['./tournament-view.component.css']
})
export class TournamentViewComponent implements OnInit {
  @Input()
  tournament: TournamentInfo;

  constructor() {
  }

  ngOnInit(): void {
  }

  onEnter() {
    console.log('entering the tournament');
  }
}
