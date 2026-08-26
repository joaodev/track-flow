import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterOutlet, RouterLinkActive } from '@angular/router';
import { Store } from '@ngrx/store';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { BreakpointObserver } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { map, shareReplay } from 'rxjs';
import { selectIsAdmin } from '../../features/auth/data-access/auth.selectors';
import { AuthActions } from '../../features/auth/data-access/auth.actions';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterOutlet,
    RouterLinkActive,
    MatIconModule,
    MatSidenavModule,
    MatToolbarModule,
    MatButtonModule,
  ],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.scss',
})
export class AppShellComponent {
  private store = inject(Store);
  private breakpointObserver = inject(BreakpointObserver);

  isAdmin$ = this.store.select(selectIsAdmin);

  isMobile = toSignal(
    this.breakpointObserver.observe('(max-width: 900px)').pipe(
      map((result) => result.matches),
      shareReplay(1),
    ),
    { initialValue: false },
  );

  onLogout(): void {
    this.store.dispatch(AuthActions.logout());
  }
}
