import { createFeature, createReducer, on } from '@ngrx/store';
import { AuthActions } from './auth.actions';
import { TOKEN_KEY } from '../../../core/token-storage.service';
import { decodeTokenRole } from '../../../core/jwt.utils';

export interface AuthState {
  token: string | null;
  role: string | null;
  loading: boolean;
  error: string | null;
}

const persistedToken = localStorage.getItem(TOKEN_KEY);

export const initialState: AuthState = {
  token: localStorage.getItem(TOKEN_KEY),
  role: decodeTokenRole(persistedToken),
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
      role: decodeTokenRole(token),
      loading: false,
    })),

    on(AuthActions.loginFailure, (state, { error }): AuthState => ({
      ...state,
      loading: false,
      error,
    })),

    on(AuthActions.logout, (): AuthState => ({ token: null, role: null, loading: false, error: null })),
  ),
});
