export interface Shipment {
  id: number;
  trackingCode: string;
  origin: string;
  destination: string;
  carrier: String;
  status: String;
  createdAt: string;
  updatedAt: string;
}

export interface TrackingEvent {
  id: number;
  shipmentId: number;
  status: string;
  location: string | null;
  description: string | null;
  occurredAt: string;
}

export interface UpdateShipmentStatusRequest {
  status: string;
  location?: string;
  description?: string;
}
