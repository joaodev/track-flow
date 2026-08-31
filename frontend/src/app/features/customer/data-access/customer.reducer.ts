import { createEntityAdapter, EntityState } from '@ngrx/entity';
import { Customer } from './customer.model';
import { createFeature, createReducer, on } from '@ngrx/store';
import { CustomerActions } from './customer.actions';

export interface CustomerState extends EntityState<Customer> {
  loading: boolean;
  error: string | null;
}

export const customerAdapter = createEntityAdapter<Customer>();

export const initialState: CustomerState = customerAdapter.getInitialState({
  loading: false,
  error: null,
});

export const customerFeature = createFeature({
  name: 'customer',
  reducer: createReducer(
    initialState,

    on(
      CustomerActions.loadCustomers,
      CustomerActions.createCustomer,
      CustomerActions.updateCustomer,
      CustomerActions.setActive,
      CustomerActions.deleteCustomer,
      (state): CustomerState => ({ ...state, loading: true, error: null }),
    ),

    on(CustomerActions.loadCustomersSuccess, (state, { customers }): CustomerState =>
      customerAdapter.setAll(customers, { ...state, loading: false }),
    ),

    on(CustomerActions.createCustomerSuccess, (state, { customer }): CustomerState =>
      customerAdapter.addOne(customer, { ...state, loading: false }),
    ),

    on(
      CustomerActions.updateCustomerSuccess,
      CustomerActions.setActiveSuccess,
      (state, { customer }): CustomerState =>
        customerAdapter.upsertOne(customer, { ...state, loading: false }),
    ),

    on(CustomerActions.deleteCustomerSuccess, (state, { id }): CustomerState =>
      customerAdapter.removeOne(id, { ...state, loading: false }),
    ),

    on(
      CustomerActions.loadCustomersFailure,
      CustomerActions.createCustomerFailure,
      CustomerActions.updateCustomerFailure,
      CustomerActions.setActiveFailure,
      CustomerActions.deleteCustomerFailure,
      (state, { error }): CustomerState => ({ ...state, loading: false, error }),
    ),
  ),
});
