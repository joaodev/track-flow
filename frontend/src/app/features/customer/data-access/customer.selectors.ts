import { customerAdapter, customerFeature } from './customer.reducer';
import { createSelector } from '@ngrx/store';

export const { selectCustomerState, selectLoading, selectError } = customerFeature;

const { selectAll } = customerAdapter.getSelectors();

export const selectAllCustomers = createSelector(selectCustomerState, selectAll);

export const selectActiveCustomers = createSelector(selectAllCustomers, (customers) =>
  customers.filter((c) => c.active),
);

export const selectCustomerStats = createSelector(selectAllCustomers, (customers) => ({
  total: customers.length,
  active: customers.filter((c) => c.active).length,
}));
