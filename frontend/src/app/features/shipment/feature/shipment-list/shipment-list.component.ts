import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { ShipmentTableComponent } from '../../ui/shipment-table/shipment-table.component';
import { StatCardComponent } from '../../ui/stat-card/stat-card.component';
import { CreateShipmentFormComponent } from '../../ui/create-shipment-form/create-shipment-form.component';
import { UpdateStatusFormComponent } from '../../ui/update-status-form/update-status-form.component';
import { ShipmentDetailDialogComponent } from '../../ui/shipment-detail-dialog/shipment-detail-dialog.component';
import { ShipmentActions } from '../../data-access/shipment.actions';
import {
  selectAllShipments,
  selectError,
  selectLoading,
  selectShipmentStats,
} from '../../data-access/shipment.selectors';
import {
  CreateShipmentRequest,
  Shipment,
  UpdateShipmentStatusRequest,
} from '../../data-access/shipment.model';
import { selectIsAdmin } from '../../../auth/data-access/auth.selectors';
import { ConfirmDialogComponent } from '../../../../shared/confirm-dialog/confirm-dialog.component';
import { TranslatePipe } from '../../../../core/translate.pipe';
import { TranslationService } from '../../../../core/translation.service';

@Component({
  selector: 'app-shipment-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, ShipmentTableComponent, StatCardComponent, TranslatePipe],
  templateUrl: './shipment-list.component.html',
  styleUrl: './shipment-list.component.scss',
})
export class ShipmentListComponent implements OnInit {
  private store = inject(Store);
  private dialog = inject(MatDialog);
  private translation = inject(TranslationService);

  shipments$ = this.store.select(selectAllShipments);
  loading$ = this.store.select(selectLoading);
  error$ = this.store.select(selectError);
  stats$ = this.store.select(selectShipmentStats);
  isAdmin$ = this.store.select(selectIsAdmin);

  ngOnInit(): void {
    this.store.dispatch(ShipmentActions.loadShipments());
  }

  onOpenCreateForm(): void {
    const dialogRef = this.dialog.open(CreateShipmentFormComponent, { width: '420px' });

    dialogRef.afterClosed().subscribe((request: CreateShipmentRequest | undefined) => {
      if (request) {
        this.store.dispatch(ShipmentActions.createShipment({ request }));
      }
    });
  }

  onUpdateStatus(shipment: Shipment): void {
    const dialogRef = this.dialog.open(UpdateStatusFormComponent, {
      data: { trackingCode: shipment.trackingCode, currentStatus: shipment.status },
      width: '400px',
    });

    dialogRef.afterClosed().subscribe((request: UpdateShipmentStatusRequest | undefined) => {
      if (request) {
        this.store.dispatch(
          ShipmentActions.updateShipmentStatus({ trackingCode: shipment.trackingCode, request }),
        );
      }
    });
  }

  onViewDetails(shipment: Shipment): void {
    this.dialog.open(ShipmentDetailDialogComponent, { data: { shipment }, width: '480px' });
  }

  onDeleteShipment(shipment: Shipment): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete shipment',
        message: `Are you sure you want to delete shipment ${shipment.trackingCode}? This cannot be undone.`,
      },
      width: '400px',
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean | undefined) => {
      if (confirmed) {
        this.store.dispatch(
          ShipmentActions.deleteShipment({ trackingCode: shipment.trackingCode }),
        );
      }
    });
  }
}
