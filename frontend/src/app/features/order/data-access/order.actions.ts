import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { CreateOrderRequest, Order, OrderItem, ShipOrderRequest } from './order.model';

export const OrderActions = createActionGroup({
  source: 'Order',
  events: {
    'Load Orders': emptyProps(),
    'Load Orders Success': props<{ orders: Order[] }>(),
    'Load Orders Failure': props<{ error: string }>(),

    'Load Order Items': props<{ orderId: number }>(),
    'Load Order Items Success': props<{ orderId: number; items: OrderItem[] }>(),
    'Load Order Items Failure': props<{ error: string }>(),

    'Create Order': props<{ request: CreateOrderRequest }>(),
    'Create Order Success': props<{ order: Order }>(),
    'Create Order Failure': props<{ error: string }>(),

    'Confirm Order': props<{ id: number }>(),
    'Confirm Order Success': props<{ order: Order }>(),
    'Confirm Order Failure': props<{ error: string }>(),

    'Ship Order': props<{ id: number; request: ShipOrderRequest }>(),
    'Ship Order Success': props<{ order: Order }>(),
    'Ship Order Failure': props<{ error: string }>(),

    'Cancel Order': props<{ id: number }>(),
    'Cancel Order Success': props<{ order: Order }>(),
    'Cancel Order Failure': props<{ error: string }>(),

    'Delete Order': props<{ id: number }>(),
    'Delete Order Success': props<{ id: number }>(),
    'Delete Order Failure': props<{ error: string }>(),
  },
});
