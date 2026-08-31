import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, mergeMap, of } from 'rxjs';
import { CustomerService } from './customer.service';
import { CustomerActions } from './customer.actions';
import { extractErrorCode } from '../../../core/http-error.utils';

@Injectable()
export class CustomerEffects {
  private actions$ = inject(Actions);
  private customerService = inject(CustomerService);

  loadCustomers$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CustomerActions.loadCustomers),
      mergeMap(() =>
        this.customerService.getAll().pipe(
          map((customers) => CustomerActions.loadCustomersSuccess({ customers })),
          catchError((error) =>
            of(CustomerActions.loadCustomersFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  createCustomer$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CustomerActions.createCustomer),
      mergeMap(({ request }) =>
        this.customerService.create(request).pipe(
          map((customer) => CustomerActions.createCustomerSuccess({ customer })),
          catchError((error) =>
            of(CustomerActions.createCustomerFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  updateCustomer$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CustomerActions.updateCustomer),
      mergeMap(({ id, request }) =>
        this.customerService.update(id, request).pipe(
          map((customer) => CustomerActions.updateCustomerSuccess({ customer })),
          catchError((error) =>
            of(CustomerActions.updateCustomerFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  setActive$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CustomerActions.setActive),
      mergeMap(({ id, active }) =>
        (active ? this.customerService.activate(id) : this.customerService.deactivate(id)).pipe(
          map((customer) => CustomerActions.setActiveSuccess({ customer })),
          catchError((error) =>
            of(CustomerActions.setActiveFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  deleteCustomer$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CustomerActions.deleteCustomer),
      mergeMap(({ id }) =>
        this.customerService.delete(id).pipe(
          map(() => CustomerActions.deleteCustomerSuccess({ id })),
          catchError((error) =>
            of(CustomerActions.deleteCustomerFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );
}
