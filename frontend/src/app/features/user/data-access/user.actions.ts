import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { CreateUserRequest, UpdateRoleRequest, User } from './user.model';

export const UserActions = createActionGroup({
  source: 'User',
  events: {
    'Load Users': emptyProps(),
    'Load Users Success': props<{ users: User[] }>(),
    'Load Users Failure': props<{ error: string }>(),

    'Create User': props<{ request: CreateUserRequest }>(),
    'Create User Success': props<{ user: User }>(),
    'Create User Failure': props<{ error: string }>(),

    'Change Role': props<{ id: number; request: UpdateRoleRequest }>(),
    'Change Role Success': props<{ user: User }>(),
    'Change Role Failure': props<{ error: string }>(),

    'Set Active': props<{ id: number; active: boolean }>(),
    'Set Active Success': props<{ user: User }>(),
    'Set Active Failure': props<{ error: string }>(),
  },
});
