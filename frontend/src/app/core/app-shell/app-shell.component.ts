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
import { TranslatePipe } from '../translate.pipe';
import { Lang, TranslationService } from '../translation.service';

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
    TranslatePipe,
  ],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.scss',
})
export class AppShellComponent {
  private store = inject(Store);
  private breakpointObserver = inject(BreakpointObserver);
  private translation = inject(TranslationService);

  isAdmin$ = this.store.select(selectIsAdmin);
  currentLang = this.translation.currentLang;
  languages: Lang[] = ['pt', 'en', 'es'];

  isMobile = toSignal(
    this.breakpointObserver.observe('(max-width: 900px)').pipe(
      map((result) => result.matches),
      shareReplay(1),
    ),
    { initialValue: false },
  );

  setLang(lang: Lang): void {
    this.translation.setLang(lang);
  }

  onLogout(): void {
    this.store.dispatch(AuthActions.logout());
  }
}
