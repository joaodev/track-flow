import { productAdapter, productFeature } from './product.reducer';
import { createSelector } from '@ngrx/store';

export const { selectProductState, selectLoading, selectError } = productFeature;

const { selectAll } = productAdapter.getSelectors();

export const selectAllProducts = createSelector(selectProductState, selectAll);

export const selectActiveProducts = createSelector(selectAllProducts, (products) =>
  products.filter((p) => p.active),
);
