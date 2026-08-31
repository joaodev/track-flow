import { carrierAdapter, carrierFeature } from './carrier.reducer';
import { createSelector } from '@ngrx/store';

export const { selectCarrierState, selectLoading, selectError } = carrierFeature;

const { selectAll } = carrierAdapter.getSelectors();

export const selectAllCarriers = createSelector(selectCarrierState, selectAll);

export const selectActiveCarriers = createSelector(selectAllCarriers, (carriers) =>
  carriers.filter((c) => c.active),
);

export const selectCarrierStats = createSelector(selectAllCarriers, (carriers) => ({
  total: carriers.length,
  active: carriers.filter((c) => c.active).length,
}));
