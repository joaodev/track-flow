export interface Order {
  id: number;
  orderNumber: string;
  customerId: number;
  origin: string;
  destination: string;
  status: string; // PENDING | CONFIRMED | SHIPPED | DELIVERED | CANCELLED
  shipmentId: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  id: number;
  orderId: number;
  productId: number;
  quantity: number;
  unitPriceAtOrder: number;
}

export interface OrderItemRequest {
  productId: number;
  quantity: number;
}

export interface CreateOrderRequest {
  customerId: number;
  origin: string;
  destination: string;
  items: OrderItemRequest[];
}

export interface ShipOrderRequest {
  carrierId: number;
}
