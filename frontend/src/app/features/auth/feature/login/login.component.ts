import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginFormComponent } from '../../../shipment/ui/login-form/login-form.component';
import { Store } from '@ngrx/store';
import { selectError, selectLoading } from '../../data-access/auth.selectors';
import { LoginRequest } from '../../data-access/auth.model';
import { AuthActions } from '../../data-access/auth.actions';
import { MatIcon } from '@angular/material/icon';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, LoginFormComponent, MatIcon],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private store = inject(Store);

  loading$ = this.store.select(selectLoading);
  error$ = this.store.select(selectError);

  onLogin(request: LoginRequest): void {
    this.store.dispatch(AuthActions.login({ request }));
  }
}
