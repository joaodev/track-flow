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

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: ShipmentListComponent },
      { path: 'track', component: TrackShipmentComponent },
      { path: 'products', component: ProductManagementComponent },
      { path: 'orders', component: OrderManagementComponent },
      { path: 'admin/users', component: UserManagementComponent, canActivate: [adminGuard] },
    ],
  },
];
