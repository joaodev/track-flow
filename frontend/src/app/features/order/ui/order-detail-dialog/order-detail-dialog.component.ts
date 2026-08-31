import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { combineLatest, map } from 'rxjs';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { OrderActions } from '../../data-access/order.actions';
import { selectItemsForOrder } from '../../data-access/order.selectors';
import { Order } from '../../data-access/order.model';
import { selectAllProducts } from '../../../product/data-access/product.selectors';
import { TranslatePipe } from '../../../../core/translate.pipe';
import { LocalCurrencyPipe } from '../../../../core/currency/local-currency.pipe';

export interface OrderDetailDialogData {
  order: Order;
}

@Component({
  selector: 'app-order-detail-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, TranslatePipe, LocalCurrencyPipe],
  templateUrl: './order-detail-dialog.component.html',
  styleUrl: './order-detail-dialog.component.scss',
})
export class OrderDetailDialogComponent implements OnInit {
  data = inject<OrderDetailDialogData>(MAT_DIALOG_DATA);
  private store = inject(Store);

  private items$ = this.store.select(selectItemsForOrder(this.data.order.id));
  private products$ = this.store.select(selectAllProducts);

  itemsWithProduct$ = combineLatest([this.items$, this.products$]).pipe(
    map(([items, products]) =>
      items.map((item) => ({
        ...item,
        productName: products.find((p) => p.id === item.productId)?.name ?? `#${item.productId}`,
      })),
    ),
  );

  ngOnInit(): void {
    this.store.dispatch(OrderActions.loadOrderItems({ orderId: this.data.order.id }));
  }
}
