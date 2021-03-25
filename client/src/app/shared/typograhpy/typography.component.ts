import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-mat-typography',
  template: `
    <p>
      <a href="https://material.angular.io/guide/typography" target="_blank">Material typography</a>
    </p>
    <div class="mat-display-4">This is display-4</div>
    <div class="mat-display-3">This is display-3</div>
    <div class="mat-display-2">This is display-2</div>
    <div class="mat-display-1">This is display-1</div>
    <div class="mat-headline">This is mat-headline (h1)</div>
    <div class="mat-title">This is mat-title (h2)</div>
    <div class="mat-subheading-2">This is mat-subheading-2 (h3)</div>
    <div class="mat-subheading-1">This is mat-subheading-1 (h4)</div>
    <div class="mat-body-1">This is mat-body-1</div>
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
