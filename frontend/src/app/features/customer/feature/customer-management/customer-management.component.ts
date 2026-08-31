import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { CustomerTableComponent } from '../../ui/customer-table/customer-table.component';
import { CustomerFormComponent } from '../../ui/customer-form/customer-form.component';
import { CustomerActions } from '../../data-access/customer.actions';
import { selectAllCustomers, selectError } from '../../data-access/customer.selectors';
import { Customer, CustomerRequest } from '../../data-access/customer.model';
import { selectIsAdmin } from '../../../auth/data-access/auth.selectors';
import { ConfirmDialogComponent } from '../../../../shared/confirm-dialog/confirm-dialog.component';
import { TranslationService } from '../../../../core/translation.service';
import { TranslatePipe } from '../../../../core/translate.pipe';

@Component({
  selector: 'app-customer-management',
  standalone: true,
  imports: [CommonModule, MatButtonModule, CustomerTableComponent, TranslatePipe],
  templateUrl: './customer-management.component.html',
  styleUrl: './customer-management.component.scss',
})
export class CustomerManagementComponent implements OnInit {
  private store = inject(Store);
  private dialog = inject(MatDialog);
  private translation = inject(TranslationService);

  customers$ = this.store.select(selectAllCustomers);
  error$ = this.store.select(selectError);
  isAdmin$ = this.store.select(selectIsAdmin);

  ngOnInit(): void {
    this.store.dispatch(CustomerActions.loadCustomers());
  }

  onOpenCreateForm(): void {
    const dialogRef = this.dialog.open(CustomerFormComponent, { width: '420px' });

    dialogRef.afterClosed().subscribe((request: CustomerRequest | undefined) => {
      if (request) {
        this.store.dispatch(CustomerActions.createCustomer({ request }));
      }
    });
  }

  onEditCustomer(customer: Customer): void {
    const dialogRef = this.dialog.open(CustomerFormComponent, {
      data: { customer },
      width: '420px',
    });

    dialogRef.afterClosed().subscribe((request: CustomerRequest | undefined) => {
      if (request) {
        this.store.dispatch(CustomerActions.updateCustomer({ id: customer.id, request }));
      }
    });
  }

  onToggleActive(event: { id: number; active: boolean }): void {
    this.store.dispatch(CustomerActions.setActive(event));
  }

  onDeleteCustomer(customer: Customer): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.translation.t('customer.deleteConfirm.title'),
        message: this.translation.t('customer.deleteConfirm.message', { name: customer.name }),
      },
      width: '400px',
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean | undefined) => {
      if (confirmed) {
        this.store.dispatch(CustomerActions.deleteCustomer({ id: customer.id }));
      }
    });
  }
}
