import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-mat-typography',
  template: `
    <p>
      <a href="https://material.angular.io/guide/typography" target="_blank">Material typography</a>
    </p>
    <div class="mat-headline-1">This is mat-headline-1</div>
    <div class="mat-headline-2">This is mat-headline-2</div>
    <div class="mat-headline-3">This is mat-headline-3</div>
    <div class="mat-headline-4">This is mat-headline-4</div>
    <div class="mat-headline-5">This is mat-headline-5 (h1)</div>
    <div class="mat-headline-6">This is mat-headline-6 (h2)</div>
    <div class="mat-subtitle-1">This is mat-subtitle-2 (h3)</div>
    <div class="mat-subtitle-2">This is mat-subtitle-2</div>
    <div class="mat-body-1">This is mat-body-1 (h4)</div>
    <div class="mat-body-2">This is mat-body-2</div>
    <div class="mat-caption">This is mat-caption</div>
    <p>This is p</p>
    <h1>This is h1</h1>
    <h2>This is h2</h2>
    <h3>This is h3</h3>
    <h4>This is h4</h4>
    <h5>This is h5</h5>
    <h6>This is h6</h6>
  `,
  styles: ['div, p, h1, h2, h3, h4, h5, h6 { border: 1px solid black; }']
})
export class TypographyComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
