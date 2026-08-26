import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterOutlet, RouterLinkActive } from '@angular/router';
import { Store } from '@ngrx/store';
import { MatIconModule } from '@angular/material/icon';
import { selectIsAdmin } from '../../features/auth/data-access/auth.selectors';
import { AuthActions } from '../../features/auth/data-access/auth.actions';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterOutlet, RouterLinkActive, MatIconModule],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.scss',
})
export class AppShellComponent {
  private store = inject(Store);

  isAdmin$ = this.store.select(selectIsAdmin);

  onLogout(): void {
    this.store.dispatch(AuthActions.logout());
  }
}
