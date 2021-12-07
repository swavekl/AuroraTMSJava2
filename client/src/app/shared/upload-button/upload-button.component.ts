import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FileRepositoryService} from './file-repository.service';
import {HttpEventType} from '@angular/common/http';

@Component({
  selector: 'app-upload-file-component',
  template: `
    <div fxLayout="row" fxLayoutAlign="start start" fxLayoutGap="10px">
      <input
        style="display: none"
        type="file" (change)="onFileChanged($event)"
        #fileInput>
      <button mat-raised-button type="button" [disabled]="disabledBtn" (click)="fileInput.click()">
        {{selectButtonLabel}}</button>
      <button mat-raised-button type="button" [disabled]="!selectedFile" (click)="onUpload()">
        {{uploadButtonLabel}}</button>
      <mat-progress-spinner [style.display]="!inProgress ? 'none' : 'block'"
                            [value]="percentComplete"
                            [diameter]="36"
                            mode="determinate"
                            strokeWidth="5">
      </mat-progress-spinner>
      <mat-icon [style.display]="!done ? 'none' : 'block'"
                style="font-size: 36px; color: green; font-weight: bold; padding-right: 12px;" >
        done</mat-icon>
    </div>
  `,
  styles: [
  ]
})
export class UploadButtonComponent implements OnInit {

  // where should this file be stored in file repository
  @Input()
  public storagePath: string;

  @Input()
  public disabledBtn: boolean;

  @Input()
  public selectButtonLabel = 'Select File';

  @Input()
  public uploadButtonLabel = 'Upload';

  // file selected by user
  public selectedFile: File;

  public inProgress: boolean;

  public done: boolean;

  public percentComplete: number;

  @Output()
  public uploadFinished: EventEmitter<string>;

  constructor(private fileRepository: FileRepositoryService) {
    this.disabledBtn = false;
    this.inProgress = false;
    this.done = false;
    this.uploadFinished = new EventEmitter<string>();
  }

  ngOnInit(): void {
  }

  onFileChanged(event) {
    this.selectedFile = event.target.files[0];
  }

  onUpload() {
    this.inProgress = true;
    this.done = false;
    this.percentComplete = 0;
    if (this.selectedFile) {
      this.fileRepository.upload(this.selectedFile, this.storagePath)
        .subscribe((response: any) => {
          console.log('response', response);
          if (response.type === HttpEventType.UploadProgress) {
            this.percentComplete = Math.round(response.loaded / response.total * 100);
          } else if (response.type === HttpEventType.Response) {
            this.inProgress = false;
            this.done = true;
            this.selectedFile = null;
            if (response.ok) {
              const downloadUrl = response.headers.get('location');
              this.uploadFinished.emit(downloadUrl);
            }
          }
        });
    }
  }
}
