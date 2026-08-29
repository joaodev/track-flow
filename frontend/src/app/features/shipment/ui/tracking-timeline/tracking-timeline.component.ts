import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrackingEvent } from '../../data-access/shipment.model';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '../../../../core/translate.pipe';

@Component({
  selector: 'app-tracking-timeline',
  standalone: true,
  imports: [CommonModule, MatIcon, TranslatePipe],
  templateUrl: './tracking-timeline.component.html',
  styleUrl: './tracking-timeline.component.scss',
})
export class TrackingTimelineComponent {
  @Input({ required: true }) events: TrackingEvent[] = [];
}
