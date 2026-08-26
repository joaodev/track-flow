import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrackingEvent } from '../../data-access/shipment.model';
import { MatIcon } from '@angular/material/icon';

@Component({
  selector: 'app-tracking-timeline',
  standalone: true,
  imports: [CommonModule, MatIcon],
  templateUrl: './tracking-timeline.component.html',
  styleUrl: './tracking-timeline.component.scss',
})
export class TrackingTimelineComponent {
  @Input({ required: true }) events: TrackingEvent[] = [];
}
