import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Shipment } from '../../data-access/shipment.model';
import { MatIcon } from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';
import { TranslatePipe } from '../../../../core/translate.pipe';

@Component({
  selector: 'app-shipment-table',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIcon,
    MatTooltip,
    TranslatePipe,
  ],
  templateUrl: './shipment-table.component.html',
  styleUrl: './shipment-table.component.scss',
})
export class ShipmentTableComponent implements OnChanges, AfterViewInit {
  @Input({ required: true }) shipments: Shipment[] = [];
  @Input() loading = false;
  @Output() updateStatus = new EventEmitter<Shipment>();
  @Output() viewDetails = new EventEmitter<Shipment>();
  @Input() isAdmin = false;
  @Output() deleteShipment = new EventEmitter<Shipment>();

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = [
    'trackingCode',
    'origin',
    'destination',
    'carrier',
    'status',
    'updatedAt',
    'actions',
  ];
  dataSource = new MatTableDataSource<Shipment>([]);
  recentlyUpdated = new Set<string>();

  private previousUpdatedAt = new Map<string, string>();
  private viewReady = false;

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.viewReady = true;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['shipments']) return;

    for (const shipment of this.shipments) {
      const previous = this.previousUpdatedAt.get(shipment.trackingCode);
      if (previous && previous !== shipment.updatedAt) {
        this.recentlyUpdated.add(shipment.trackingCode);
        setTimeout(() => this.recentlyUpdated.delete(shipment.trackingCode), 1000);
      }
      this.previousUpdatedAt.set(shipment.trackingCode, shipment.updatedAt);
    }

    this.dataSource.data = this.shipments;
    if (this.viewReady) {
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
    }
  }

  applyFilter(value: string): void {
    this.dataSource.filter = value.trim().toLowerCase();
  }
}
