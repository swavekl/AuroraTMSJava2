import {EventEmitter} from '@angular/core';
import {Directive, HostListener, OnDestroy, OnInit, Output} from '@angular/core';
import {Subject, Subscription} from 'rxjs';
import {debounceTime} from 'rxjs/operators';

/**
 * Directive for debouncing multiple clicks to avoid submitting requests
 */
@Directive({
  selector: '[debounceClicks]'
})
export class DebounceClicksDirective implements OnInit, OnDestroy {
  @Output() debounceClick = new EventEmitter();
  private clicks = new Subject();
  private subscription: Subscription;

  constructor() {
  }

  ngOnInit() {
    this.subscription = this.clicks
      .pipe(debounceTime(500))
      .subscribe(e => this.debounceClick.emit(e));
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  @HostListener('click', ['$event'])
  clickEvent(event) {
    event.preventDefault();
    event.stopPropagation();
    this.clicks.next(event);
  }
}
