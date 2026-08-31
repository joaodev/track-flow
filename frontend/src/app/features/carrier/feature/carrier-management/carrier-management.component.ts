import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { CarrierTableComponent } from '../../ui/carrier-table/carrier-table.component';
import { CarrierFormComponent } from '../../ui/carrier-form/carrier-form.component';
import { CarrierActions } from '../../data-access/carrier.actions';
import { selectAllCarriers, selectError } from '../../data-access/carrier.selectors';
import { Carrier, CarrierRequest } from '../../data-access/carrier.model';
import { selectIsAdmin } from '../../../auth/data-access/auth.selectors';
import { ConfirmDialogComponent } from '../../../../shared/confirm-dialog/confirm-dialog.component';
import { TranslationService } from '../../../../core/translation.service';
import { TranslatePipe } from '../../../../core/translate.pipe';

@Component({
  selector: 'app-carrier-management',
  standalone: true,
  imports: [CommonModule, MatButtonModule, CarrierTableComponent, TranslatePipe],
  templateUrl: './carrier-management.component.html',
  styleUrl: './carrier-management.component.scss',
})
export class CarrierManagementComponent implements OnInit {
  private store = inject(Store);
  private dialog = inject(MatDialog);
  private translation = inject(TranslationService);

  carriers$ = this.store.select(selectAllCarriers);
  error$ = this.store.select(selectError);
  isAdmin$ = this.store.select(selectIsAdmin);

  ngOnInit(): void {
    this.store.dispatch(CarrierActions.loadCarriers());
  }

  onOpenCreateForm(): void {
    const dialogRef = this.dialog.open(CarrierFormComponent, { width: '420px' });

    dialogRef.afterClosed().subscribe((request: CarrierRequest | undefined) => {
      if (request) {
        this.store.dispatch(CarrierActions.createCarrier({ request }));
      }
    });
  }

  onEditCarrier(carrier: Carrier): void {
    const dialogRef = this.dialog.open(CarrierFormComponent, { data: { carrier }, width: '420px' });

    dialogRef.afterClosed().subscribe((request: CarrierRequest | undefined) => {
      if (request) {
        this.store.dispatch(CarrierActions.updateCarrier({ id: carrier.id, request }));
      }
    });
  }

  onToggleActive(event: { id: number; active: boolean }): void {
    this.store.dispatch(CarrierActions.setActive(event));
  }

  onDeleteCarrier(carrier: Carrier): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.translation.t('carrier.deleteConfirm.title'),
        message: this.translation.t('carrier.deleteConfirm.message', { name: carrier.name }),
      },
      width: '400px',
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean | undefined) => {
      if (confirmed) {
        this.store.dispatch(CarrierActions.deleteCarrier({ id: carrier.id }));
      }
    });
  }
}
