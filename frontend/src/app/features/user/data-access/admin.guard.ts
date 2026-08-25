import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { Store } from '@ngrx/store';

import { map, take } from 'rxjs';
import { selectIsAdmin } from '../../auth/data-access/auth.selectors';

export const adminGuard: CanActivateFn = () => {
  const store = inject(Store);
  const router = inject(Router);

  return store.select(selectIsAdmin).pipe(
    take(1),
    map((isAdmin) => isAdmin || router.createUrlTree(['/']))
  );
};
