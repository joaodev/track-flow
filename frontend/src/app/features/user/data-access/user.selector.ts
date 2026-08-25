import { userAdapter, userFeature } from './user.reducer';
import { createSelector } from '@ngrx/store';

export const {
  selectUserState,
  selectLoading: selectUsersLoading,
  selectError: selectUsersError,
} = userFeature;

export const selectAllUsers = createSelector(
  selectUserState, userAdapter.getSelectors().selectAll
);
