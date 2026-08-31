import { createEntityAdapter, EntityState } from '@ngrx/entity';
import { Order, OrderItem } from './order.model';
import { createFeature, createReducer, on } from '@ngrx/store';
import { OrderActions } from './order.actions';

export interface OrderState extends EntityState<Order> {
  loading: boolean;
  error: string | null;
  itemsByOrderId: Record<number, OrderItem[]>;
}

export const orderAdapter = createEntityAdapter<Order>();

export const initialState: OrderState = orderAdapter.getInitialState({
  loading: false,
  error: null,
  itemsByOrderId: {},
});

export const orderFeature = createFeature({
  name: 'order',
  reducer: createReducer(
    initialState,

    on(
      OrderActions.loadOrders,
      OrderActions.createOrder,
      OrderActions.confirmOrder,
      OrderActions.shipOrder,
      OrderActions.cancelOrder,
      OrderActions.deleteOrder,
      (state): OrderState => ({ ...state, loading: true, error: null }),
    ),

    on(OrderActions.loadOrdersSuccess, (state, { orders }): OrderState =>
      orderAdapter.setAll(orders, { ...state, loading: false }),
    ),

    on(OrderActions.createOrderSuccess, (state, { order }): OrderState =>
      orderAdapter.addOne(order, { ...state, loading: false }),
    ),

    on(
      OrderActions.confirmOrderSuccess,
      OrderActions.shipOrderSuccess,
      OrderActions.cancelOrderSuccess,
      (state, { order }): OrderState => orderAdapter.upsertOne(order, { ...state, loading: false }),
    ),

    on(OrderActions.deleteOrderSuccess, (state, { id }): OrderState =>
      orderAdapter.removeOne(id, { ...state, loading: false }),
    ),

    on(OrderActions.loadOrderItemsSuccess, (state, { orderId, items }): OrderState => ({
      ...state,
      itemsByOrderId: { ...state.itemsByOrderId, [orderId]: items },
    })),

    on(
      OrderActions.loadOrdersFailure,
      OrderActions.createOrderFailure,
      OrderActions.confirmOrderFailure,
      OrderActions.shipOrderFailure,
      OrderActions.cancelOrderFailure,
      OrderActions.deleteOrderFailure,
      OrderActions.loadOrderItemsFailure,
      (state, { error }): OrderState => ({ ...state, loading: false, error }),
    ),
  ),
});
