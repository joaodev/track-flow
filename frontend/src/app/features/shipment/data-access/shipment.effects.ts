import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, mergeMap, of } from 'rxjs';
import { ShipmentService } from './shipment.service';
import { ShipmentActions } from './shipment.actions';


@Injectable()
export class ShipmentEffects {
  private actions$ = inject(Actions);
  private shipmentService = inject(ShipmentService);

  loadShipments$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ShipmentActions.loadShipments),
      mergeMap(() =>
        this.shipmentService.getAll().pipe(
          map((shipments) => ShipmentActions.loadShipmentsSuccess({ shipments })),
          catchError((error) => of(ShipmentActions.loadShipmentsFailure({ error: error.message })))
        )
      )
    )
  );

  createShipment$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ShipmentActions.createShipment),
      mergeMap(({ request }) =>
      this.shipmentService.create(request).pipe(
        map((shipment) => ShipmentActions.createShipmentSuccess({ shipment })),
        catchError((error) => of(ShipmentActions.createShipmentFailure({ error: error.message })))
      ))
    )
  );

  updateShipmentStatus$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ShipmentActions.updateShipmentStatus),
      mergeMap(({ trackingCode, request }) =>
        this.shipmentService.updateStatus(trackingCode, request).pipe(
          map((shipment) => ShipmentActions.updateShipmentStatusSuccess({ shipment })),
          catchError((error) => of(ShipmentActions.updateShipmentStatusFailure({ error: error.message })))
        )
      )
    )
  );

  loadHistory$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ShipmentActions.loadHistory),
      mergeMap(({ trackingCode }) =>
        this.shipmentService.getHistory(trackingCode).pipe(
          map((history) => ShipmentActions.loadHistorySuccess({ history })),
          catchError((error) => of(ShipmentActions.loadHistoryFailure({ error: error.message })))
        )
      )
    )
  );
}
