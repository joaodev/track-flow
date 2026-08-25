import { createFeature, createReducer, on } from '@ngrx/store';
import { AuthActions } from './auth.actions';
import { TOKEN_KEY } from '../../../core/token-storage.service';

export interface AuthState {
  token: string | null;
  loading: boolean;
  error: string | null;
}

export const initialState: AuthState = {
  token: localStorage.getItem(TOKEN_KEY),
  loading: false,
  error: null
};

export const authFeature = createFeature({
  name: 'auth',
  reducer: createReducer(
    initialState,

    on(AuthActions.login, (state): AuthState => ({ ...state, loading: true, error: null })),

    on(AuthActions.loginSuccess, (state, { token }): AuthState => ({
      ...state,
      token,
      loading: false,
    })),

    on(AuthActions.loginFailure, (state, { error }): AuthState => ({
      ...state,
      loading: false,
      error
    })),

    on(AuthActions.loginSuccess, (state, { token }): AuthState => ({
      ...state,
      token
    })),

    on(AuthActions.logout, (): AuthState => initialState)
  ),
})
