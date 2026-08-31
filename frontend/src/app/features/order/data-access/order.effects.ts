import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, mergeMap, of } from 'rxjs';
import { OrderService } from './order.service';
import { OrderActions } from './order.actions';
import { extractErrorCode } from '../../../core/http-error.utils';

@Injectable()
export class OrderEffects {
  private actions$ = inject(Actions);
  private orderService = inject(OrderService);

  loadOrders$ = createEffect(() =>
    this.actions$.pipe(
      ofType(OrderActions.loadOrders),
      mergeMap(() =>
        this.orderService.getAll().pipe(
          map((orders) => OrderActions.loadOrdersSuccess({ orders })),
          catchError((error) =>
            of(OrderActions.loadOrdersFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  loadOrderItems$ = createEffect(() =>
    this.actions$.pipe(
      ofType(OrderActions.loadOrderItems),
      mergeMap(({ orderId }) =>
        this.orderService.getItems(orderId).pipe(
          map((items) => OrderActions.loadOrderItemsSuccess({ orderId, items })),
          catchError((error) =>
            of(OrderActions.loadOrderItemsFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  createOrder$ = createEffect(() =>
    this.actions$.pipe(
      ofType(OrderActions.createOrder),
      mergeMap(({ request }) =>
        this.orderService.create(request).pipe(
          map((order) => OrderActions.createOrderSuccess({ order })),
          catchError((error) =>
            of(OrderActions.createOrderFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  confirmOrder$ = createEffect(() =>
    this.actions$.pipe(
      ofType(OrderActions.confirmOrder),
      mergeMap(({ id }) =>
        this.orderService.confirm(id).pipe(
          map((order) => OrderActions.confirmOrderSuccess({ order })),
          catchError((error) =>
            of(OrderActions.confirmOrderFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  shipOrder$ = createEffect(() =>
    this.actions$.pipe(
      ofType(OrderActions.shipOrder),
      mergeMap(({ id, request }) =>
        this.orderService.ship(id, request).pipe(
          map((order) => OrderActions.shipOrderSuccess({ order })),
          catchError((error) =>
            of(OrderActions.shipOrderFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  cancelOrder$ = createEffect(() =>
    this.actions$.pipe(
      ofType(OrderActions.cancelOrder),
      mergeMap(({ id }) =>
        this.orderService.cancel(id).pipe(
          map((order) => OrderActions.cancelOrderSuccess({ order })),
          catchError((error) =>
            of(OrderActions.cancelOrderFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  deleteOrder$ = createEffect(() =>
    this.actions$.pipe(
      ofType(OrderActions.deleteOrder),
      mergeMap(({ id }) =>
        this.orderService.delete(id).pipe(
          map(() => OrderActions.deleteOrderSuccess({ id })),
          catchError((error) =>
            of(OrderActions.deleteOrderFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );
}
