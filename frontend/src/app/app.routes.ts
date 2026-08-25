import { Routes } from '@angular/router';
import { ShipmentListComponent } from './features/shipment/feature/shipment-list/shipment-list.component';
import { LoginComponent } from './features/auth/feature/login/login.component';
import { authGuard } from './features/auth/data-access/auth.guard';
import { TrackShipmentComponent } from './features/shipment/feature/track-shipment/track-shipment.component';
import { UserManagementComponent } from './features/user/feature/user-management/user-management.component';
import { adminGuard } from './features/user/data-access/admin.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'track', component: TrackShipmentComponent },
  { path: '', component: ShipmentListComponent, canActivate: [authGuard] },
  { path: 'admin/users', component: UserManagementComponent, canActivate: [authGuard, adminGuard] },
];
