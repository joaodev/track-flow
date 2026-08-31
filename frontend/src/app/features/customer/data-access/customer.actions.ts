import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { Customer, CustomerRequest } from './customer.model';

export const CustomerActions = createActionGroup({
  source: 'Customer',
  events: {
    'Load Customers': emptyProps(),
    'Load Customers Success': props<{ customers: Customer[] }>(),
    'Load Customers Failure': props<{ error: string }>(),

    'Create Customer': props<{ request: CustomerRequest }>(),
    'Create Customer Success': props<{ customer: Customer }>(),
    'Create Customer Failure': props<{ error: string }>(),

    'Update Customer': props<{ id: number; request: CustomerRequest }>(),
    'Update Customer Success': props<{ customer: Customer }>(),
    'Update Customer Failure': props<{ error: string }>(),

    'Set Active': props<{ id: number; active: boolean }>(),
    'Set Active Success': props<{ customer: Customer }>(),
    'Set Active Failure': props<{ error: string }>(),

    'Delete Customer': props<{ id: number }>(),
    'Delete Customer Success': props<{ id: number }>(),
    'Delete Customer Failure': props<{ error: string }>(),
  },
});
