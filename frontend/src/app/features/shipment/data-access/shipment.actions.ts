import { createActionGroup, emptyProps, props } from '@ngrx/store';
import {
  Shipment,
  TrackingEvent,
  UpdateShipmentStatusRequest,
} from './shipment.model';
import { ShipmentStatusChangedEvent } from './shipment-status-changed-event.model';

export const ShipmentActions = createActionGroup({
  source: 'Shipment',
  events: {
    'Load Shipments': emptyProps(),
    'Load Shipments Success': props<{ shipments: Shipment[] }>(),
    'Load Shipments Failure': props<{ error: string }>(),

    'Load Shipment': props<{ trackingCode: string }>(),
    'Load Shipment Success': props<{ shipment: Shipment }>(),
    'Load Shipment Failure': props<{ error: string }>(),

    'Update Shipment Status': props<{
      trackingCode: string;
      request: UpdateShipmentStatusRequest;
    }>(),
    'Update Shipment Status Success': props<{ shipment: Shipment }>(),
    'Update Shipment Status Failure': props<{ error: string }>(),
    'Shipment Status Received': props<{ event: ShipmentStatusChangedEvent }>(),

    'Load History': props<{ trackingCode: string }>(),
    'Load History Success': props<{ history: TrackingEvent[] }>(),
    'Load History Failure': props<{ error: string }>(),

    'Delete Shipment': props<{ trackingCode: string }>(),
    'Delete Shipment Success': props<{ trackingCode: string }>(),
    'Delete Shipment Failure': props<{ error: string }>(),
    'Shipment Deleted Received': props<{ trackingCode: string }>(),
  },
});
