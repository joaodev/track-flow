import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { take } from 'rxjs';
import { StatCardComponent } from '../../../../shared/stat-card/stat-card.component';
import { TranslatePipe } from '../../../../core/translate.pipe';

import { ShipmentActions } from '../../../shipment/data-access/shipment.actions';
import { selectShipmentStats } from '../../../shipment/data-access/shipment.selectors';

import { ProductActions } from '../../../product/data-access/product.actions';
import { selectProductStats } from '../../../product/data-access/product.selectors';

import { InventoryActions } from '../../../inventory/data-access/inventory.actions';
import { selectLowStockCount } from '../../../inventory/data-access/inventory.selectors';

import { OrderActions } from '../../../order/data-access/order.actions';
import { selectOrderStats } from '../../../order/data-access/order.selectors';

import { CustomerActions } from '../../../customer/data-access/customer.actions';
import { selectCustomerStats } from '../../../customer/data-access/customer.selectors';

import { CarrierActions } from '../../../carrier/data-access/carrier.actions';
import { selectCarrierStats } from '../../../carrier/data-access/carrier.selectors';

import { UserActions } from '../../../user/data-access/user.actions';
import { selectUserStats } from '../../../user/data-access/user.selector';

import { selectIsAdmin } from '../../../auth/data-access/auth.selectors';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, StatCardComponent, TranslatePipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private store = inject(Store);

  shipmentStats$ = this.store.select(selectShipmentStats);
  productStats$ = this.store.select(selectProductStats);
  lowStockCount$ = this.store.select(selectLowStockCount);
  orderStats$ = this.store.select(selectOrderStats);
  customerStats$ = this.store.select(selectCustomerStats);
  carrierStats$ = this.store.select(selectCarrierStats);
  userStats$ = this.store.select(selectUserStats);
  isAdmin$ = this.store.select(selectIsAdmin);

  ngOnInit(): void {
    this.store.dispatch(ShipmentActions.loadShipments());
    this.store.dispatch(ProductActions.loadProducts());
    this.store.dispatch(InventoryActions.loadInventory());
    this.store.dispatch(OrderActions.loadOrders());
    this.store.dispatch(CustomerActions.loadCustomers());
    this.store.dispatch(CarrierActions.loadCarriers());

    // GET /api/users is admin-only — dispatching it for OPS staff would
    // just generate a guaranteed 403 on every dashboard load, for nothing.
    this.isAdmin$.pipe(take(1)).subscribe((isAdmin) => {
      if (isAdmin) {
        this.store.dispatch(UserActions.loadUsers());
      }
    });
  }
}
