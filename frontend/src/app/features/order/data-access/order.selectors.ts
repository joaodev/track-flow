import { orderAdapter, orderFeature } from './order.reducer';
import { createSelector } from '@ngrx/store';

export const { selectOrderState, selectLoading, selectError, selectItemsByOrderId } = orderFeature;

const { selectAll } = orderAdapter.getSelectors();

export const selectAllOrders = createSelector(selectOrderState, selectAll);

export const selectItemsForOrder = (orderId: number) =>
  createSelector(selectItemsByOrderId, (itemsByOrderId) => itemsByOrderId[orderId] ?? []);

export const selectOrderStats = createSelector(selectAllOrders, (orders) => ({
  total: orders.length,
  pending: orders.filter((o) => o.status === 'PENDING').length,
  confirmed: orders.filter((o) => o.status === 'CONFIRMED').length,
  shipped: orders.filter((o) => o.status === 'SHIPPED').length,
  delivered: orders.filter((o) => o.status === 'DELIVERED').length,
  cancelled: orders.filter((o) => o.status === 'CANCELLED').length,
}));
