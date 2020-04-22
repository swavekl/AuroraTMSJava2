import {Component, Input, OnInit} from '@angular/core';
import {TournamentInfo} from '../tournament-info.model';

@Component({
  selector: 'app-tournament-list',
  templateUrl: './tournament-list.component.html',
  styleUrls: ['./tournament-list.component.css']
})
export class TournamentListComponent implements OnInit {
  @Input()
  tournaments: TournamentInfo [];

  constructor() {
  }

  ngOnInit(): void {
  }

}
