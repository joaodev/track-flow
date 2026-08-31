import { createEntityAdapter, EntityState } from '@ngrx/entity';
import { Shipment, TrackingEvent } from './shipment.model';
import { createFeature, createReducer, on } from '@ngrx/store';
import { ShipmentActions } from './shipment.actions';

export interface ShipmentState extends EntityState<Shipment> {
  history: TrackingEvent[];
  loading: boolean;
  error: string | null;
}

export const shipmentAdapter = createEntityAdapter<Shipment>({
  selectId: (shipment) => shipment.trackingCode,
});

export const initialState: ShipmentState = shipmentAdapter.getInitialState({
  history: [],
  loading: false,
  error: null,
});

export const shipmentFeature = createFeature({
  name: 'shipment',
  reducer: createReducer(
    initialState,

    on(
      ShipmentActions.loadShipments,
      ShipmentActions.updateShipmentStatus,
      ShipmentActions.loadHistory,
      (state): ShipmentState => ({ ...state, loading: true, error: null }),
    ),

    on(ShipmentActions.loadShipmentsSuccess, (state, { shipments }): ShipmentState =>
      shipmentAdapter.setAll(shipments, { ...state, loading: false }),
    ),

    on(ShipmentActions.loadShipment, (state): ShipmentState => ({
      ...state,
      loading: true,
      error: null,
    })),

    on(ShipmentActions.loadShipmentSuccess, (state, { shipment }): ShipmentState =>
      shipmentAdapter.upsertOne(shipment, { ...state, loading: false }),
    ),

    on(ShipmentActions.loadShipmentFailure, (state, { error }): ShipmentState => ({
      ...state,
      loading: false,
      error,
    })),

    on(ShipmentActions.updateShipmentStatusSuccess, (state, { shipment }): ShipmentState =>
      shipmentAdapter.upsertOne(shipment, { ...state, loading: false }),
    ),

    on(ShipmentActions.shipmentStatusReceived, (state, { event }): ShipmentState =>
      shipmentAdapter.updateOne(
        {
          id: event.trackingCode,
          changes: {
            status: event.newStatus,
            updatedAt: event.occurredAt,
          },
        },
        state,
      ),
    ),

    on(ShipmentActions.loadHistorySuccess, (state, { history }): ShipmentState => ({
      ...state,
      history,
      loading: false,
    })),

    on(ShipmentActions.deleteShipment, (state): ShipmentState => ({
      ...state,
      loading: true,
      error: null,
    })),

    on(ShipmentActions.deleteShipmentSuccess, (state, { trackingCode }): ShipmentState =>
      shipmentAdapter.removeOne(trackingCode, { ...state, loading: false }),
    ),

    on(ShipmentActions.shipmentDeletedReceived, (state, { trackingCode }): ShipmentState =>
      shipmentAdapter.removeOne(trackingCode, state),
    ),

    on(
      ShipmentActions.loadShipmentsFailure,
      ShipmentActions.updateShipmentStatusFailure,
      ShipmentActions.loadHistoryFailure,
      ShipmentActions.deleteShipmentFailure,
      (state, { error }): ShipmentState => ({
        ...state,
        loading: false,
        error,
      }),
    ),
  ),
});
