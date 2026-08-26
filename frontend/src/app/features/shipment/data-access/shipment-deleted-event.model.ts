import { ShipmentStatusChangedEvent } from './shipment-status-changed-event.model';

export interface ShipmentDeletedEvent {
  shipmentId: number;
  trackingCode: string;
  deletedAt: string;
}

export type ShipmentSocketEvent = ShipmentStatusChangedEvent | ShipmentDeletedEvent;

export function isShipmentDeletedEvent(event: ShipmentSocketEvent): event is ShipmentDeletedEvent {
  return 'deletedAt' in event;
}
