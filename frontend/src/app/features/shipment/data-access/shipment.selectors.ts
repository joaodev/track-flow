import { shipmentAdapter, shipmentFeature } from './shipment.reducer';
import { createSelector } from '@ngrx/store';

export const { selectShipmentState, selectHistory, selectLoading, selectError} = shipmentFeature;

const { selectAll } = shipmentAdapter.getSelectors();

export const selectAllShipments = createSelector(selectShipmentState, selectAll);

export const selectShipmentByTrackingCode = (trackingCode: string) =>
  createSelector(selectShipmentState, (state) =>
    shipmentAdapter.getSelectors().selectEntities(state)[trackingCode]);
