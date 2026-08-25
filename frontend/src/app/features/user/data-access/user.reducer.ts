import { createEntityAdapter, EntityState } from '@ngrx/entity';
import { User } from './user.model';
import { createFeature, createReducer, on } from '@ngrx/store';
import { UserActions } from './user.actions';

export interface UserState extends EntityState<User> {
  loading: boolean;
  error: string | null;
}

export const userAdapter = createEntityAdapter<User>();

export const initialState: UserState = userAdapter.getInitialState({
  loading: false, error: null
});

export const userFeature = createFeature({
  name: 'user',
  reducer: createReducer(
    initialState,

    on(
      UserActions.loadUsers,
      UserActions.createUser,
      UserActions.changeRole,
      (state): UserState => ({ ...state, loading: true, error: null })
    ),

    on(UserActions.loadUsersSuccess, (state, { users }): UserState =>
      userAdapter.setAll(users, { ...state, loading: false })
    ),

    on(UserActions.createUserSuccess, UserActions.setActiveSuccess, (state, { user }): UserState =>
      userAdapter.addOne(user, { ...state, loading: false })
    ),

    on(UserActions.changeRoleSuccess, UserActions.setActiveSuccess, (state, { user }): UserState =>
      userAdapter.upsertOne(user, { ...state, loading: false })
    ),

    on(
      UserActions.loadUsersFailure,
      UserActions.createUserFailure,
      UserActions.changeRoleFailure,
      UserActions.setActiveFailure,
      (state, { error }): UserState => ({ ...state, loading: false, error })
    ),
  )
});
