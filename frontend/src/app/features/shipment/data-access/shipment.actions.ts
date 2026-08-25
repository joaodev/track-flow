import { createActionGroup, emptyProps, props } from '@ngrx/store';
import {
  CreateShipmentRequest,
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

    'Create Shipment': props<{ request: CreateShipmentRequest }>(),
    'Create Shipment Success': props<{ shipment: Shipment }>(),
    'Create Shipment Failure': props<{ error: string }>(),

    'Update Shipment Status': props<{ trackingCode: string; request: UpdateShipmentStatusRequest }>(),
    'Update Shipment Status Success': props<{ shipment: Shipment }>(),
    'Update Shipment Status Failure': props<{ error: string }>(),
    'Shipment Status Received': props<{ event: ShipmentStatusChangedEvent }>(),

    'Load History': props<{ trackingCode: string }>(),
    'Load History Success': props<{ history: TrackingEvent[] }>(),
    'Load History Failure': props<{ error: string }>()
  },
});
