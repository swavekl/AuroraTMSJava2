import {Component, Input, OnInit} from '@angular/core';
import {Tournament} from '../../tournament/tournament-config/tournament.model';

@Component({
  selector: 'app-matches-landing',
  templateUrl: './matches-landing.component.html',
  styleUrls: ['./matches-landing.component.scss']
})
export class MatchesLandingComponent implements OnInit {

  @Input()
  tournaments: Tournament[] = [];

  constructor() { }

  ngOnInit(): void {
  }

}
