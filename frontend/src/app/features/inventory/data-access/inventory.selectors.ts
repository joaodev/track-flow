import { inventoryAdapter, inventoryFeature } from './inventory.reducer';
import { createSelector } from '@ngrx/store';

export const { selectInventoryState, selectLoading, selectError, selectLastAlert } =
  inventoryFeature;

const { selectEntities } = inventoryAdapter.getSelectors();

// Dictionary keyed by productId — { [productId]: Inventory }, ready to
// look up directly from the product table without another pass over the array.
export const selectInventoryByProductId = createSelector(selectInventoryState, selectEntities);
