import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-password-strength',
  templateUrl: './password-strength.component.html',
  styleUrls: ['./password-strength.component.scss']
})
export class PasswordStrengthComponent {
  @Input() password!: string;

  isLongEnough() {
    if (!this.password) {
      return false;
    } else {
      return this.password?.length >= 8;
    }
  }

  hasUpperCaseCharacters() {
    if (!this.password) {
      return false;
    } else {
      return this.password.match(/[A-Z]+/);
    }
  }

  hasLowerCaseCharacters() {
    if (!this.password) {
      return false;
    } else {
      return this.password.match(/[a-z]+/);
    }
  }

  hasDigits() {
    if (!this.password) {
      return false;
    } else {
      return this.password.match(/[0-9]+/);
    }
  }

  hasSymbol() {
    if (!this.password) {
      return false;
    } else {
      return this.password.match(/[~!@#\$%\^&\*\-+\?]+/);
    }
  }
}
