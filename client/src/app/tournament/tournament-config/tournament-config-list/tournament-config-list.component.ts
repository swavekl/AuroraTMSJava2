import {Component, Input, OnInit} from '@angular/core';
import {Tournament} from '../tournament.model';

@Component({
  selector: 'app-tournament-config-list',
  templateUrl: './tournament-config-list.component.html',
  styleUrls: ['./tournament-config-list.component.css']
})
export class TournamentConfigListComponent implements OnInit {

  @Input()
  tournaments: Tournament[];

  constructor() {
  }

  ngOnInit(): void {
  }

}
