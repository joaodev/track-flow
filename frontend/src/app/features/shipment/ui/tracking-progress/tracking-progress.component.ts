import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

const STAGES = [
  { key: 'CREATED', label: 'Created', icon: 'inventory_2' },
  { key: 'IN_TRANSIT', label: 'In Transit', icon: 'local_shipping' },
  { key: 'DELIVERED', label: 'Delivered', icon: 'check_circle' },
];

@Component({
  selector: 'app-tracking-progress',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './tracking-progress.component.html',
  styleUrl: './tracking-progress.component.scss',
})
export class TrackingProgressComponent {
  @Input({ transform: (value: String): string => value.toString(), required: true }) status!: string;

  stages = STAGES;

  get currentIndex(): number {
    return this.stages.findIndex((s) => s.key === this.status);
  }

  get isCancelled(): boolean {
    return this.status === 'CANCELLED';
  }
}
