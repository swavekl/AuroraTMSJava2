import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-sematch',
  templateUrl: './sematch.component.html',
  styleUrls: ['./sematch.component.scss']
})
export class SEMatchComponent implements OnInit {

  @Input()
  match: any;

  constructor() { }

  ngOnInit(): void {
  }

}
