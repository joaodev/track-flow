import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Store } from '@ngrx/store';
import { TokenStorageService } from './core/token-storage.service';
import { AuthActions } from './features/auth/data-access/auth.actions';

@Component({
  imports: [RouterOutlet],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  private store = inject(Store);
  private tokenStorage = inject(TokenStorageService);

  ngOnInit(): void {
    const token = this.tokenStorage.getToken();
    if (token) {
      this.store.dispatch(AuthActions.restoreSession({ token }));
    }
  }
}
