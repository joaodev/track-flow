import { createEntityAdapter, EntityState } from '@ngrx/entity';
import { Carrier } from './carrier.model';
import { createFeature, createReducer, on } from '@ngrx/store';
import { CarrierActions } from './carrier.actions';

export interface CarrierState extends EntityState<Carrier> {
  loading: boolean;
  error: string | null;
}

export const carrierAdapter = createEntityAdapter<Carrier>();

export const initialState: CarrierState = carrierAdapter.getInitialState({
  loading: false,
  error: null,
});

export const carrierFeature = createFeature({
  name: 'carrier',
  reducer: createReducer(
    initialState,

    on(
      CarrierActions.loadCarriers,
      CarrierActions.createCarrier,
      CarrierActions.updateCarrier,
      CarrierActions.setActive,
      CarrierActions.deleteCarrier,
      (state): CarrierState => ({ ...state, loading: true, error: null }),
    ),

    on(CarrierActions.loadCarriersSuccess, (state, { carriers }): CarrierState =>
      carrierAdapter.setAll(carriers, { ...state, loading: false }),
    ),

    on(CarrierActions.createCarrierSuccess, (state, { carrier }): CarrierState =>
      carrierAdapter.addOne(carrier, { ...state, loading: false }),
    ),

    on(
      CarrierActions.updateCarrierSuccess,
      CarrierActions.setActiveSuccess,
      (state, { carrier }): CarrierState =>
        carrierAdapter.upsertOne(carrier, { ...state, loading: false }),
    ),

    on(CarrierActions.deleteCarrierSuccess, (state, { id }): CarrierState =>
      carrierAdapter.removeOne(id, { ...state, loading: false }),
    ),

    on(
      CarrierActions.loadCarriersFailure,
      CarrierActions.createCarrierFailure,
      CarrierActions.updateCarrierFailure,
      CarrierActions.setActiveFailure,
      CarrierActions.deleteCarrierFailure,
      (state, { error }): CarrierState => ({ ...state, loading: false, error }),
    ),
  ),
});
