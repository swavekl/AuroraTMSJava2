import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TournamentInfo} from '../tournament-info.model';
import {Regions} from '../../../shared/regions';
import {MatButtonToggleChange} from '@angular/material/button-toggle';

@Component({
  selector: 'app-tournament-list',
  templateUrl: './tournament-list.component.html',
  styleUrls: ['./tournament-list.component.css']
})
export class TournamentListComponent implements OnInit {
  @Input()
  tournaments: TournamentInfo [];

  @Input()
  selectedRegion: string;

  regions: any [] = new Regions().getList();

  @Output()
  filterChange: EventEmitter<string> = new EventEmitter<string>();

  constructor() {
  }

  ngOnInit(): void {
  }

  onRegionChange($event: MatButtonToggleChange) {
    this.selectedRegion = $event.value;
    this.filterChange.emit($event.value);
  }
}
