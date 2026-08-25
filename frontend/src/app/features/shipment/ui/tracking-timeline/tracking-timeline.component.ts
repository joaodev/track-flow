import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrackingEvent } from '../../data-access/shipment.model';

@Component({
  selector: 'app-tracking-timeline',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tracking-timeline.component.html',
})
export class TrackingTimelineComponent {
  @Input({ required: true }) events: TrackingEvent[] = [];
}
