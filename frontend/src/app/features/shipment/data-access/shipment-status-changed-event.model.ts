export interface ShipmentStatusChangedEvent {
  shipmentId: number;
  trackingCode: string;
  previousStatus: string;
  newStatus: string;
  location: string | null;
  description: string | null;
  occurredAt: string;
}
