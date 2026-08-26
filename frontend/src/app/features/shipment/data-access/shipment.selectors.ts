import { shipmentAdapter, shipmentFeature } from './shipment.reducer';
import { createSelector } from '@ngrx/store';

export const { selectShipmentState, selectHistory, selectLoading, selectError} = shipmentFeature;

const { selectAll } = shipmentAdapter.getSelectors();

export const selectAllShipments = createSelector(selectShipmentState, selectAll);

export const selectShipmentByTrackingCode = (trackingCode: string) =>
  createSelector(selectShipmentState, (state) =>
    shipmentAdapter.getSelectors().selectEntities(state)[trackingCode]);

export const selectShipmentStats = createSelector(selectAllShipments, (shipments) => ({
  total: shipments.length,
  inTransit: shipments.filter((s) => s.status === 'IN_TRANSIT').length,
  delivered: shipments.filter((s) => s.status === 'DELIVERED').length,
  cancelled: shipments.filter((s) => s.status === 'CANCELLED').length,
}));
