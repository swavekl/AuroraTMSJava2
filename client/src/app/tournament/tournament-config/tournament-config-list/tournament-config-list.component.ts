import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Tournament} from '../tournament.model';

@Component({
  selector: 'app-tournament-config-list',
  templateUrl: './tournament-config-list.component.html',
  styleUrls: ['./tournament-config-list.component.scss']
})
export class TournamentConfigListComponent implements OnInit {

  @Input()
  tournaments: Tournament[];

  @Output()
  add: EventEmitter<any> = new EventEmitter<any>();

  constructor() {
  }

  ngOnInit(): void {
  }

  addTournament() {
    this.add.emit('add');
  }
}
