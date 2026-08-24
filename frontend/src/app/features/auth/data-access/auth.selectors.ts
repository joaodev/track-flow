import { authFeature } from './auth.reducer';
import { createSelector } from '@ngrx/store';

export const { selectToken, selectLoading, selectError } = authFeature;

export const selectIsAuthenticated = createSelector(selectToken, (token) => !!token);
