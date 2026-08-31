import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, mergeMap, merge, of } from 'rxjs';
import { ShipmentService } from './shipment.service';
import { ShipmentActions } from './shipment.actions';
import { ShipmentSocketService } from './shipment-socket.service';
import { isShipmentDeletedEvent, ShipmentSocketEvent } from './shipment-deleted-event.model';
import { extractErrorCode } from '../../../core/http-error.utils';



@Injectable()
export class ShipmentEffects {
  private actions$ = inject(Actions);
  private shipmentService = inject(ShipmentService);
  private socketService = inject(ShipmentSocketService);

  loadShipments$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ShipmentActions.loadShipments),
      mergeMap(() =>
        this.shipmentService.getAll().pipe(
          map((shipments) => ShipmentActions.loadShipmentsSuccess({ shipments })),
          catchError((error) => of(ShipmentActions.loadShipmentsFailure({
            error: extractErrorCode(error)
          }))),
        ),
      ),
    ),
  );

  updateShipmentStatus$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ShipmentActions.updateShipmentStatus),
      mergeMap(({ trackingCode, request }) =>
        this.shipmentService.updateStatus(trackingCode, request).pipe(
          map((shipment) => ShipmentActions.updateShipmentStatusSuccess({ shipment })),
          catchError((error) =>
            of(ShipmentActions.updateShipmentStatusFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  loadHistory$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ShipmentActions.loadHistory),
      mergeMap(({ trackingCode }) =>
        this.shipmentService.getHistory(trackingCode).pipe(
          map((history) => ShipmentActions.loadHistorySuccess({ history })),
          catchError((error) => of(ShipmentActions.loadHistoryFailure({
            error: extractErrorCode(error)
          }))),
        ),
      ),
    ),
  );

  liveStatusUpdate$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ShipmentActions.loadShipmentsSuccess),
      mergeMap(({ shipments }) =>
        merge(...shipments.map((shipment) => this.socketService.watch(shipment.trackingCode))),
      ),
      map((event) => this.toShipmentAction(event)),
    ),
  );

  deleteShipment$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ShipmentActions.deleteShipment),
      mergeMap(({ trackingCode }) =>
        this.shipmentService.delete(trackingCode).pipe(
          map(() => ShipmentActions.deleteShipmentSuccess({ trackingCode })),
          catchError((error) =>
            of(
              ShipmentActions.deleteShipmentFailure({
                error: extractErrorCode(error)
              }),
            ),
          ),
        ),
      ),
    ),
  );

  private toShipmentAction(event: ShipmentSocketEvent) {
    return isShipmentDeletedEvent(event)
      ? ShipmentActions.shipmentDeletedReceived({ trackingCode: event.trackingCode })
      : ShipmentActions.shipmentStatusReceived({ event });
  }

  loadShipment$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ShipmentActions.loadShipment),
      mergeMap(({ trackingCode }) =>
        this.shipmentService.getByTrackingCode(trackingCode).pipe(
          map((shipment) => ShipmentActions.loadShipmentSuccess({ shipment })),
          catchError((error) =>
            of(
              ShipmentActions.loadShipmentFailure({
                error: extractErrorCode(error)
              }),
            ),
          ),
        ),
      ),
    ),
  );

  watchLoadedShipment$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ShipmentActions.loadShipmentSuccess),
      mergeMap(({ shipment }) => this.socketService.watch(shipment.trackingCode)),
      map((event) => this.toShipmentAction(event)),
    ),
  );
}
