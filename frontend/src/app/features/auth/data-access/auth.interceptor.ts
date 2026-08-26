import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { catchError, throwError } from 'rxjs';
import { TokenStorageService } from '../../../core/token-storage.service';
import { AuthActions } from './auth.actions';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStorage = inject(TokenStorageService);
  const store = inject(Store);
  const token = tokenStorage.getToken();

  if (!token || !req.url.startsWith('/api')) {
    return next(req);
  }

  const authReq = req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  });

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        store.dispatch(AuthActions.logout());
      }
      return throwError(() => error);
    }),
  );
};
