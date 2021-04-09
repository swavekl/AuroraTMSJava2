import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-centered-panel',
  templateUrl: './centered-panel.component.html',
  styleUrls: ['./centered-panel.component.css']
})
export class CenteredPanelComponent implements OnInit {

  @Input()
  noTopGap: boolean;

  constructor() {
    this.noTopGap = false;
  }

  ngOnInit(): void {
  }

}
