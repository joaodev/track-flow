import { orderAdapter, orderFeature } from './order.reducer';
import { createSelector } from '@ngrx/store';

export const { selectOrderState, selectLoading, selectError, selectItemsByOrderId } = orderFeature;

const { selectAll } = orderAdapter.getSelectors();

export const selectAllOrders = createSelector(selectOrderState, selectAll);

export const selectItemsForOrder = (orderId: number) =>
  createSelector(selectItemsByOrderId, (itemsByOrderId) => itemsByOrderId[orderId] ?? []);
