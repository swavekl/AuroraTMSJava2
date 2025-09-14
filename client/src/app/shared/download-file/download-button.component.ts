import {Component, Input, OnInit} from '@angular/core';
import {FileRepositoryService} from '../upload-button/file-repository.service';

@Component({
    selector: 'app-download-file',
    template: `
    <button mat-raised-button type="button" (click)="onDownload()" [disabled]="disabledBtn" style="margin: 0 10px">
      {{ buttonLabel }}
    </button>
  `,
    styles: [],
    standalone: false
})
export class DownloadButtonComponent implements OnInit {
  @Input()
  public fileUrl: string;

  @Input()
  public buttonLabel: string;

  @Input()
  public disabledBtn: boolean;

  constructor(private fileRepositoryService: FileRepositoryService) {
    this.buttonLabel = 'Download';
    this.disabledBtn = false;
  }

  ngOnInit(): void {
  }

  onDownload() {
    this.fileRepositoryService.download(this.fileUrl);
  }
}
