import {Component} from '@angular/core';
import {DomSanitizer} from '@angular/platform-browser';
import {MatIconRegistry} from '@angular/material/icon';

/**
 * @title SVG icons
 */
@Component({
  selector: 'app-connect-with-stripe-icon',
  template: '<mat-icon style="height: 36px; width: 170px" class="mat-raised-button" svgIcon="connect-with-stripe" inline="true" aria-hidden="false" aria-label="Connect with Stripe icon"></mat-icon>',
})
export class ConnectWithStripeIconComponent {
  constructor(iconRegistry: MatIconRegistry, sanitizer: DomSanitizer) {
    iconRegistry.addSvgIcon('connect-with-stripe', sanitizer.bypassSecurityTrustResourceUrl('../assets/images/connectWithStripe.svg'));
  }
}
