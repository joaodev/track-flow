import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, mergeMap, of } from 'rxjs';
import { InventoryService } from './inventory.service';
import { InventorySocketService } from './inventory-socket.service';
import { InventoryActions } from './inventory.actions';
import { extractErrorCode } from '../../../core/http-error.utils';
import { ProductActions } from '../../product/data-access/product.actions';

@Injectable()
export class InventoryEffects {
  private actions$ = inject(Actions);
  private inventoryService = inject(InventoryService);
  private socketService = inject(InventorySocketService);

  loadInventory$ = createEffect(() =>
    this.actions$.pipe(
      ofType(InventoryActions.loadInventory),
      mergeMap(() =>
        this.inventoryService.getAll().pipe(
          map((inventory) => InventoryActions.loadInventorySuccess({ inventory })),
          catchError((error) =>
            of(InventoryActions.loadInventoryFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  adjustStock$ = createEffect(() =>
    this.actions$.pipe(
      ofType(InventoryActions.adjustStock),
      mergeMap(({ productId, request }) =>
        this.inventoryService.adjustStock(productId, request).pipe(
          map((inventory) => InventoryActions.adjustStockSuccess({ inventory })),
          catchError((error) =>
            of(InventoryActions.adjustStockFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  updateThreshold$ = createEffect(() =>
    this.actions$.pipe(
      ofType(InventoryActions.updateThreshold),
      mergeMap(({ productId, request }) =>
        this.inventoryService.updateThreshold(productId, request).pipe(
          map((inventory) => InventoryActions.updateThresholdSuccess({ inventory })),
          catchError((error) =>
            of(InventoryActions.updateThresholdFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  watchLowStock$ = createEffect(() =>
    this.socketService
      .watch()
      .pipe(map((event) => InventoryActions.lowStockAlertReceived({ event }))),
  );

  refreshOnProductCreated$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ProductActions.createProductSuccess),
      map(() => InventoryActions.loadInventory()),
    ),
  );
}
