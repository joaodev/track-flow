import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, mergeMap, of } from 'rxjs';
import { CarrierService } from './carrier.service';
import { CarrierActions } from './carrier.actions';
import { extractErrorCode } from '../../../core/http-error.utils';

@Injectable()
export class CarrierEffects {
  private actions$ = inject(Actions);
  private carrierService = inject(CarrierService);

  loadCarriers$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CarrierActions.loadCarriers),
      mergeMap(() =>
        this.carrierService.getAll().pipe(
          map((carriers) => CarrierActions.loadCarriersSuccess({ carriers })),
          catchError((error) =>
            of(CarrierActions.loadCarriersFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  createCarrier$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CarrierActions.createCarrier),
      mergeMap(({ request }) =>
        this.carrierService.create(request).pipe(
          map((carrier) => CarrierActions.createCarrierSuccess({ carrier })),
          catchError((error) =>
            of(CarrierActions.createCarrierFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  updateCarrier$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CarrierActions.updateCarrier),
      mergeMap(({ id, request }) =>
        this.carrierService.update(id, request).pipe(
          map((carrier) => CarrierActions.updateCarrierSuccess({ carrier })),
          catchError((error) =>
            of(CarrierActions.updateCarrierFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  setActive$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CarrierActions.setActive),
      mergeMap(({ id, active }) =>
        (active ? this.carrierService.activate(id) : this.carrierService.deactivate(id)).pipe(
          map((carrier) => CarrierActions.setActiveSuccess({ carrier })),
          catchError((error) =>
            of(CarrierActions.setActiveFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  deleteCarrier$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CarrierActions.deleteCarrier),
      mergeMap(({ id }) =>
        this.carrierService.delete(id).pipe(
          map(() => CarrierActions.deleteCarrierSuccess({ id })),
          catchError((error) =>
            of(CarrierActions.deleteCarrierFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );
}
