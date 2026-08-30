import { createEntityAdapter, EntityState } from '@ngrx/entity';
import { Inventory, LowStockEvent } from './inventory.model';
import { createFeature, createReducer, on } from '@ngrx/store';
import { InventoryActions } from './inventory.actions';

export interface InventoryState extends EntityState<Inventory> {
  loading: boolean;
  error: string | null;
  lastAlert: LowStockEvent | null;
}

export const inventoryAdapter = createEntityAdapter<Inventory>({
  selectId: (inventory) => inventory.productId,
});

export const initialState: InventoryState = inventoryAdapter.getInitialState({
  loading: false,
  error: null,
  lastAlert: null,
});

export const inventoryFeature = createFeature({
  name: 'inventory',
  reducer: createReducer(
    initialState,

    on(InventoryActions.loadInventory, (state): InventoryState => ({
      ...state,
      loading: true,
      error: null,
    })),

    on(InventoryActions.loadInventorySuccess, (state, { inventory }): InventoryState =>
      inventoryAdapter.setAll(inventory, { ...state, loading: false }),
    ),

    on(
      InventoryActions.adjustStockSuccess,
      InventoryActions.updateThresholdSuccess,
      (state, { inventory }): InventoryState =>
        inventoryAdapter.upsertOne(inventory, { ...state, loading: false }),
    ),

    on(
      InventoryActions.loadInventoryFailure,
      InventoryActions.adjustStockFailure,
      InventoryActions.updateThresholdFailure,
      (state, { error }): InventoryState => ({ ...state, loading: false, error }),
    ),

    on(InventoryActions.lowStockAlertReceived, (state, { event }): InventoryState => ({
      ...state,
      lastAlert: event,
    })),
  ),
});
