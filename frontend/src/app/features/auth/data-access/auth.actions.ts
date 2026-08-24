import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { LoginRequest } from './auth.model';

export const AuthActions = createActionGroup({
  source: 'Auth',
  events: {
    'Login': props<{ request: LoginRequest }>(),
    'Login Success': props<{ token: string }>(),
    'Login Failure': props<{ error: string }>(),
    'Restore Session': props<{ token: string }>(),
    'Logout': emptyProps(),
  },
})
