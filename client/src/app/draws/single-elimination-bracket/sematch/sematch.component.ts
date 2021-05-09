import {Component, Input, OnInit, ViewEncapsulation} from '@angular/core';
import {Match} from '../../model/match.model';

@Component({
  selector: 'app-sematch',
  templateUrl: './sematch.component.html',
  styleUrls: ['./sematch.component.scss'],
  // Need to remove view encapsulation so that the custom tooltip style defined in
  // `tooltip-custom-class-example.css` will not be scoped to this component's view.
  encapsulation: ViewEncapsulation.None,
})
export class SEMatchComponent implements OnInit {

  @Input()
  match: Match;

  constructor() { }

  ngOnInit(): void {
  }

}
