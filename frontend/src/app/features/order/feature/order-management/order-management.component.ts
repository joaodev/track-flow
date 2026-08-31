import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { OrderTableComponent } from '../../ui/order-table/order-table.component';
import { OrderFormComponent } from '../../ui/order-form/order-form.component';
import { ShipOrderFormComponent } from '../../ui/ship-order-form/ship-order-form.component';
import { OrderDetailDialogComponent } from '../../ui/order-detail-dialog/order-detail-dialog.component';
import { OrderActions } from '../../data-access/order.actions';
import { selectAllOrders, selectError } from '../../data-access/order.selectors';
import { CreateOrderRequest, Order, ShipOrderRequest } from '../../data-access/order.model';
import { ProductActions } from '../../../product/data-access/product.actions';
import { TranslatePipe } from '../../../../core/translate.pipe';

@Component({
  selector: 'app-order-management',
  standalone: true,
  imports: [CommonModule, MatButtonModule, OrderTableComponent, TranslatePipe],
  templateUrl: './order-management.component.html',
  styleUrl: './order-management.component.scss',
})
export class OrderManagementComponent implements OnInit {
  private store = inject(Store);
  private dialog = inject(MatDialog);

  orders$ = this.store.select(selectAllOrders);
  error$ = this.store.select(selectError);

  ngOnInit(): void {
    this.store.dispatch(OrderActions.loadOrders());
    // The order form needs the product catalog for its item picker.
    this.store.dispatch(ProductActions.loadProducts());
  }

  onOpenCreateForm(): void {
    const dialogRef = this.dialog.open(OrderFormComponent, { width: '520px' });

    dialogRef.afterClosed().subscribe((request: CreateOrderRequest | undefined) => {
      if (request) {
        this.store.dispatch(OrderActions.createOrder({ request }));
      }
    });
  }

  onViewDetails(order: Order): void {
    this.dialog.open(OrderDetailDialogComponent, { data: { order }, width: '480px' });
  }

  onConfirmOrder(order: Order): void {
    this.store.dispatch(OrderActions.confirmOrder({ id: order.id }));
  }

  onShipOrder(order: Order): void {
    const dialogRef = this.dialog.open(ShipOrderFormComponent, { width: '400px' });

    dialogRef.afterClosed().subscribe((request: ShipOrderRequest | undefined) => {
      if (request) {
        this.store.dispatch(OrderActions.shipOrder({ id: order.id, request }));
      }
    });
  }

  onCancelOrder(order: Order): void {
    this.store.dispatch(OrderActions.cancelOrder({ id: order.id }));
  }
}
