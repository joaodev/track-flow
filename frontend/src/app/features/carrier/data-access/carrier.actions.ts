import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { Carrier, CarrierRequest } from './carrier.model';

export const CarrierActions = createActionGroup({
  source: 'Carrier',
  events: {
    'Load Carriers': emptyProps(),
    'Load Carriers Success': props<{ carriers: Carrier[] }>(),
    'Load Carriers Failure': props<{ error: string }>(),

    'Create Carrier': props<{ request: CarrierRequest }>(),
    'Create Carrier Success': props<{ carrier: Carrier }>(),
    'Create Carrier Failure': props<{ error: string }>(),

    'Update Carrier': props<{ id: number; request: CarrierRequest }>(),
    'Update Carrier Success': props<{ carrier: Carrier }>(),
    'Update Carrier Failure': props<{ error: string }>(),

    'Set Active': props<{ id: number; active: boolean }>(),
    'Set Active Success': props<{ carrier: Carrier }>(),
    'Set Active Failure': props<{ error: string }>(),

    'Delete Carrier': props<{ id: number }>(),
    'Delete Carrier Success': props<{ id: number }>(),
    'Delete Carrier Failure': props<{ error: string }>(),
  },
});
