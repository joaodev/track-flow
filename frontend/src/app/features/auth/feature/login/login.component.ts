import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { selectError, selectLoading } from '../../data-access/auth.selectors';
import { LoginRequest } from '../../data-access/auth.model';
import { AuthActions } from '../../data-access/auth.actions';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '../../../../core/translate.pipe';
import { LoginFormComponent } from '../../ui/login-form/login-form.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, LoginFormComponent, MatIcon, TranslatePipe],
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
