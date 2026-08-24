import { Routes } from '@angular/router';
import { ShipmentListComponent } from './features/shipment/feature/shipment-list/shipment-list.component';
import { LoginComponent } from './features/auth/feature/login/login.component';
import { authGuard } from './features/auth/data-access/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', component: ShipmentListComponent, canActivate: [authGuard] },
];
