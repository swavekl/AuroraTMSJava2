import {Component, Input, OnInit, TemplateRef, ViewEncapsulation} from '@angular/core';
import {Match} from '../../model/match.model';
import {DrawItem} from '../../model/draw-item.model';
import {ConflictRendererHelper} from '../../model/conflict-renderer-helper.model';

@Component({
    selector: 'app-sematch',
    templateUrl: './sematch.component.html',
    styleUrls: ['./sematch.component.scss'],
    // Need to remove view encapsulation so that the custom tooltip style defined in
    // `tooltip-custom-class-example.css` will not be scoped to this component's view.
    encapsulation: ViewEncapsulation.None,
    standalone: false
})
export class SEMatchComponent implements OnInit {

  @Input()
  match: Match;

  @Input()
  doublesEvent: boolean;

  @Input()
  externalDragPreview: TemplateRef<any>;

  // inside sematch.component.ts
  @Input() highlightedSlot: 'A' | 'B' | null = null;

  constructor() { }

  ngOnInit(): void {
  }

  getConflictClass(drawItem: DrawItem) {
    return ConflictRendererHelper.getConflictClass(drawItem?.conflictType);
  }

  getConflictTooltipText(drawItem: DrawItem) {
    return ConflictRendererHelper.getConflictTooltipText(drawItem?.conflictType);
  }
}
