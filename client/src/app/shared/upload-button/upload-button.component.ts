import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FileRepositoryService} from './file-repository.service';
import {HttpEventType} from '@angular/common/http';

@Component({
    selector: 'app-upload-file-component',
    template: `
    <div fxLayout="row" fxLayoutAlign="start start">
      <input
        style="display: none"
        type="file" (change)="onFileChanged($event)"
        #fileInput>
      <button mat-raised-button type="button" [disabled]="disabledBtn" (click)="fileInput.click()">
        {{selectButtonLabel}}</button>
      <button mat-raised-button type="button" [disabled]="!selectedFile" (click)="onUpload()" style="margin: 0 10px;">
        {{uploadButtonLabel}}</button>
      <!--      spacer -->
      <div style="height: 36px; width: 36px" [style.display]="state === NOT_STARTED ? 'block': 'none'"></div>
        <!--        spinner -->
      <mat-progress-spinner [style.display]="state === IN_PROGRESS ? 'block' : 'none'"
                            [value]="percentComplete"
                            [diameter]="36"
                            mode="determinate"
                            strokeWidth="5">
      </mat-progress-spinner>
     <!--      done checkmark -->
      <mat-icon [style.display]="state === DONE ? 'block' : 'none'"
                style="font-size: 36px; color: green; font-weight: bold; padding-right: 12px;" >
        done</mat-icon>
    </div>
  `,
    styles: [],
    standalone: false
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

  public percentComplete: number;

  public state: string;
  public NOT_STARTED = 'NOT_STARTED';
  public IN_PROGRESS = 'IN_PROGRESS';
  public DONE = 'DONE';

  @Output()
  public uploadFinished: EventEmitter<string>;

  constructor(private fileRepository: FileRepositoryService) {
    this.disabledBtn = false;
    this.state = this.NOT_STARTED;
    this.uploadFinished = new EventEmitter<string>();
  }

  ngOnInit(): void {
  }

  onFileChanged(event) {
    this.selectedFile = event.target.files[0];
  }

  onUpload() {
    this.percentComplete = 0;
    this.state = this.IN_PROGRESS;
    if (this.selectedFile) {
      this.fileRepository.upload(this.selectedFile, this.storagePath)
        .subscribe((response: any) => {
          if (response.type === HttpEventType.UploadProgress) {
            this.percentComplete = Math.round(response.loaded / response.total * 100);
          } else if (response.type === HttpEventType.Response) {
            this.selectedFile = null;
            this.state = this.DONE;
            if (response.ok) {
              const downloadUrl = response.headers.get('location');
              this.uploadFinished.emit(downloadUrl);
            }
          }
        });
    }
  }
}
