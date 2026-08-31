import { Routes } from '@angular/router';
import { ShipmentListComponent } from './features/shipment/feature/shipment-list/shipment-list.component';
import { LoginComponent } from './features/auth/feature/login/login.component';
import { TrackShipmentComponent } from './features/shipment/feature/track-shipment/track-shipment.component';
import { UserManagementComponent } from './features/user/feature/user-management/user-management.component';
import { authGuard } from './features/auth/data-access/auth.guard';
import { adminGuard } from './features/user/data-access/admin.guard';
import { AppShellComponent } from './core/app-shell/app-shell.component';
import { ProductManagementComponent } from './features/product/feature/product-management/product-management.component';
import { OrderManagementComponent } from './features/order/feature/order-management/order-management.component';
import {
  CustomerManagementComponent
} from './features/customer/feature/customer-management/customer-management.component';
import { CarrierManagementComponent } from './features/carrier/feature/carrier-management/carrier-management.component';
import { DashboardComponent } from './features/dashboard/feature/dashboard/dashboard.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: DashboardComponent },
      { path: 'shipments', component: ShipmentListComponent },
      { path: 'track', component: TrackShipmentComponent },
      { path: 'products', component: ProductManagementComponent },
      { path: 'orders', component: OrderManagementComponent },
      { path: 'customers', component: CustomerManagementComponent },
      { path: 'carriers', component: CarrierManagementComponent },
      { path: 'admin/users', component: UserManagementComponent, canActivate: [adminGuard] },
    ],
  },
];
