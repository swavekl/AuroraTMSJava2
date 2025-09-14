import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatchCard} from '../../matches/model/match-card.model';

@Component({
    selector: 'app-match-assignment-dialog',
    template: `
    <h2 mat-dialog-title>
      <span *ngIf="!isMove">Warning: {{conflictTables.length === 1 ? 'Table' : 'Tables'}} in Use</span>
      <span *ngIf="isMove">Move to {{ numUsedTables === 1  ? ' Another Table' : ' Other Tables'}}</span>
    </h2>
    <mat-dialog-content>
      <ng-container *ngIf="!isMove && conflictTables.length === 1">
        <p>Table {{ conflictTables }} assigned for this match is occupied. Please select another table or force to play this match on
          assigned table.</p>
      </ng-container>
      <ng-container *ngIf="!isMove && conflictTables.length > 1">
        <p>Tables {{ conflictTables }} assigned for this match are occupied. Please select other tables or force to play this match on
          assigned tables.</p>
      </ng-container>
      <ng-container *ngIf="isMove">
        <p>Please select {{ numUsedTables === 1 ? 'table' : 'tables' }} to move this match to</p>
      </ng-container>
      <form name="form" #f="ngForm" novalidate>
        <div fxLayout="row" fxLayoutGap="10px">
          <mat-form-field style="max-width: 160px;">
            <mat-label>Table 1:</mat-label>
            <mat-select name="table1" [(ngModel)]="table1" required>
              <mat-option *ngFor="let tableNum of availableTables" [value]="tableNum">{{tableNum}}</mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field *ngIf="numUsedTables > 1" style="max-width: 160px;">
            <mat-label>Table 2:</mat-label>
            <mat-select name="table2" [(ngModel)]="table2" required appValuesDifferent="table1">
              <mat-option *ngFor="let tableNum of availableTables" [value]="tableNum">{{tableNum}}</mat-option>
            </mat-select>
            <mat-error *ngIf="f.form.controls['table2']?.errors?.appValuesDifferent">Tables must be different</mat-error>
          </mat-form-field>
        </div>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions>
      <div fxLayout="row" fxLayoutAlign="space-between" style="width: 100%">
        <button mat-raised-button cdkFocusInitial type="button" (click)="onCancel()">Cancel</button>
        <button mat-raised-button type="button" (click)="onForceAssignment()">Force</button>
        <button mat-raised-button type="button" color="primary" [disabled]="!f.valid" (click)="onMoveToOtherTables()">Move</button>
      </div>
    </mat-dialog-actions>
  `,
    styles: [],
    standalone: false
})
export class MatchAssignmentDialogComponent implements OnInit {

  matchCard: MatchCard;
  availableTables: number [] = [];
  conflictTables: number [] = [];
  numUsedTables: number;
  table1: number;
  table2: number;
  isMove: boolean;

  constructor(public dialogRef: MatDialogRef<MatchAssignmentDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: MatchAssignmentDialogData) {
    this.matchCard = data?.matchCard;
    this.availableTables = data?.availableTables;
    this.conflictTables = data?.conflictTables;
    const strUsedTables = (this.matchCard.assignedTables) ? this.matchCard.assignedTables.split(',') : [];
    this.numUsedTables = strUsedTables.length;
    this.isMove = this.conflictTables?.length === 0;
  }

  ngOnInit(): void {
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel'});

  }

  onForceAssignment() {
    this.dialogRef.close({action: 'force'});
  }

  onMoveToOtherTables() {
    const useTableNums = [this.table1];
    if (this.numUsedTables > 1) {
      useTableNums.push(this.table2);
      useTableNums.sort((t1, t2) => t1 < t2 ? -1 : 1);
    }
    console.log(useTableNums);
    this.dialogRef.close({action: 'move', useTables: useTableNums});
  }
}

export interface MatchAssignmentDialogData {
  availableTables: number [];
  conflictTables: number[];
  matchCard: MatchCard;
}
