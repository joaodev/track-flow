import { authFeature } from './auth.reducer';
import { createSelector } from '@ngrx/store';

export const { selectToken, selectRole, selectLoading, selectError } = authFeature;

export const selectIsAuthenticated = createSelector(selectToken, (token) => !!token);
export const selectIsAdmin = createSelector(selectRole, (role) => role === 'ADMIN');
