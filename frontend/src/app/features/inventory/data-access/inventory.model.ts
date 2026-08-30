export interface Inventory {
  id: number;
  productId: number;
  quantityOnHand: number;
  quantityReserved: number;
  lowStockThreshold: number;
  availableQuantity: number;
  updatedAt: string;
}

export interface AdjustStockRequest {
  quantityDelta: number;
}

export interface UpdateThresholdRequest {
  lowStockThreshold: number;
}

export interface LowStockEvent {
  productId: number;
  availableQuantity: number;
  threshold: number;
  occurredAt: string;
}
