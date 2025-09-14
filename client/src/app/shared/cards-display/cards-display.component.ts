import {Component, Input} from '@angular/core';
import {CardsInfo} from './cards-info.model';

@Component({
    selector: 'app-cards-display',
    templateUrl: './cards-display.component.html',
    styleUrl: './cards-display.component.scss',
    standalone: false
})
export class CardsDisplayComponent {

  @Input()
  cardsInfo: CardsInfo;

  @Input()
  smallFormat: boolean = true;

  // if true display all cards and call callback when clicked
  // if false display only selected cards
  @Input()
  displayAll: boolean = false;

  // callback function to call on selection
  @Input()
  private callbackFn: (scope: any, cardsInfo: CardsInfo) => void;

  @Input()
  callbackFnScope: any;

  onCardClicked(cardId: string) {
    if (this.displayAll) {
      let updatedCardsInfo = JSON.parse(JSON.stringify(this.cardsInfo));
      updatedCardsInfo[cardId] = !updatedCardsInfo[cardId];
      this.cardsInfo = updatedCardsInfo;
      if (this.callbackFn != null) {
        this.callbackFn(this.callbackFnScope, this.cardsInfo);
      }
    }
  }
}
