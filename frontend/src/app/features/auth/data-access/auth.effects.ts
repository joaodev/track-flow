import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Router } from '@angular/router';
import { catchError, map, mergeMap, of, tap } from 'rxjs';
import { AuthService } from './auth.service';
import { AuthActions } from './auth.actions';
import { TokenStorageService } from '../../../core/token-storage.service';

@Injectable()
export class AuthEffects {
  private actions$ = inject(Actions);
  private authService = inject(AuthService);
  private tokenStorage = inject(TokenStorageService);
  private router = inject(Router);

  login$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.login),
      mergeMap(({ request }) =>
        this.authService.login(request).pipe(
          map(({ token }) => AuthActions.loginSuccess({ token })),
          catchError((error) => of(AuthActions.loginFailure({ error: error.message }))),
        ),
      ),
    ),
  );

  persistTokenOnLoginSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.loginSuccess),
        tap(({ token }) => {
          this.tokenStorage.setToken(token);
          this.router.navigate(['/']);
        })
      ),
    { dispatch: false }
  );

  clearTokenOnLogout$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.logout),
        tap(() => {
          this.tokenStorage.clearToken();
          this.router.navigate(['/login']);
        })
      ),
    { dispatch: false }
  );
}
