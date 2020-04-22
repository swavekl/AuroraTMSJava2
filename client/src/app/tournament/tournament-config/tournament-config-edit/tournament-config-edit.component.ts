import {Component, Input, OnInit} from '@angular/core';
import {Tournament} from '../tournament.model';

@Component({
  selector: 'app-tournament-config-edit',
  templateUrl: './tournament-config-edit.component.html',
  styleUrls: ['./tournament-config-edit.component.css']
})
export class TournamentConfigEditComponent implements OnInit {

  @Input()
  tournament: Tournament;

  constructor() { }

  ngOnInit(): void {
  }

  onEnter() {
    console.log('entering the tournament');
  }
}
