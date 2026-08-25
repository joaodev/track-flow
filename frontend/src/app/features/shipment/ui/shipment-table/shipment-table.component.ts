import { Component, EventEmitter, Input, Output } from '@angular/core';
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
})
export class ShipmentTableComponent {
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
}
