import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { MatButtonModule } from '@angular/material/button';
import { ShipmentTableComponent } from '../../ui/shipment-table/shipment-table.component';
import { CreateShipmentFormComponent } from '../../ui/create-shipment-form/create-shipment-form.component';
import { ShipmentActions } from '../../data-access/shipment.actions';
import {
  selectAllShipments,
  selectError,
  selectLoading,
} from '../../data-access/shipment.selectors';
import { CreateShipmentRequest, Shipment, UpdateShipmentStatusRequest } from '../../data-access/shipment.model';
import { MatDialog } from '@angular/material/dialog';
import { UpdateStatusFormComponent } from '../../ui/update-status-form/update-status-form.component';
import { RouterLink } from '@angular/router';
import { selectIsAdmin } from '../../../auth/data-access/auth.selectors';
import { StatCardComponent } from '../../ui/stat-card/stat-card.component';
import { selectShipmentStats } from '../../data-access/shipment.selectors';

@Component({
  selector: 'app-shipment-list',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    ShipmentTableComponent,
    CreateShipmentFormComponent,
    RouterLink,
    StatCardComponent,
  ],
  templateUrl: './shipment-list.component.html',
  styleUrl: 'shipment-list.component.scss',
})
export class ShipmentListComponent implements OnInit {
  private store = inject(Store);
  private dialog = inject(MatDialog);

  shipments$ = this.store.select(selectAllShipments);
  loading$ = this.store.select(selectLoading);
  error$ = this.store.select(selectError);
  isAdmin$ = this.store.select(selectIsAdmin);
  stats$ = this.store.select(selectShipmentStats);

  showCreateForm = signal(false);

  ngOnInit(): void {
    this.store.dispatch(ShipmentActions.loadShipments());
  }

  toggleCreateForm(): void {
    this.showCreateForm.update((value) => !value);
  }

  onCreate(request: CreateShipmentRequest): void {
    this.store.dispatch(ShipmentActions.createShipment({ request }));
    this.showCreateForm.set(false);
  }

  onUpdateStatus(shipment: Shipment): void {
    const dialogRef = this.dialog.open(UpdateStatusFormComponent, {
      data: { trackingCode: shipment.trackingCode, currentStatus: shipment.status },
      width: '400px',
    });

    dialogRef.afterClosed().subscribe((request: UpdateShipmentStatusRequest | undefined) => {
      if (request) {
        this.store.dispatch(
          ShipmentActions.updateShipmentStatus({
            trackingCode: shipment.trackingCode,
            request,
          }),
        );
      }
    });
  }
}
