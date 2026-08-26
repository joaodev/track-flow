import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Shipment } from '../../data-access/shipment.model';
import { MatCell, MatCellDef, MatColumnDef, MatHeaderCell, MatHeaderCellDef, MatHeaderRow, MatHeaderRowDef, MatRow,
  MatRowDef, MatTable } from '@angular/material/table';
import { MatButton } from '@angular/material/button';

@Component({
  selector: 'app-shipment-table',
  standalone: true,
  imports: [
    CommonModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    MatColumnDef,
    MatTable,
    MatHeaderCellDef,
    MatHeaderCell,
    MatCell,
    MatHeaderRow,
    MatRow,
    MatHeaderRowDef,
    MatRowDef,
    MatCellDef,
    MatButton,
  ],
  templateUrl: 'shipment-table.component.html',
  styleUrl: './shipment-table.component.scss',
})
export class ShipmentTableComponent implements OnChanges {
  @Input({ required: true }) shipments: Shipment[] = [];
  @Input() loading = false;
  @Output() updateStatus = new EventEmitter<Shipment>();

  displayedColumns = [
    'trackingCode',
    'origin',
    'destination',
    'carrier',
    'status',
    'updatedAt',
    'actions',
  ];

  recentlyUpdated = new Set<string>();

  private previousUpdatedAt = new Map<string, string>();

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['shipments']) return;

    for (const shipment of this.shipments) {
      const previous = this.previousUpdatedAt.get(shipment.trackingCode);
      if (previous && previous !== shipment.updatedAt) {
        this.recentlyUpdated.add(shipment.trackingCode);
        setTimeout(() => this.recentlyUpdated.delete(shipment.trackingCode), 1000);
        this.previousUpdatedAt.set(shipment.trackingCode, shipment.updatedAt);
      }
    }
  }
}
