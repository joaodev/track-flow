import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, mergeMap, of } from 'rxjs';
import { UserService } from './user.service';
import { UserActions } from './user.actions';
import { extractErrorCode } from '../../../core/http-error.utils';

@Injectable()
export class UserEffects {
  private actions$ = inject(Actions);
  private userService = inject(UserService);

  loadUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UserActions.loadUsers),
      mergeMap(() =>
        this.userService.getAll().pipe(
          map((users) => UserActions.loadUsersSuccess({ users })),
          catchError((error) =>
            of(UserActions.loadUsersFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  createUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UserActions.createUser),
      mergeMap(({ request }) =>
        this.userService.create(request).pipe(
          map((user) => UserActions.createUserSuccess({ user })),
          catchError((error) =>
            of(
              UserActions.createUserFailure({
                error: extractErrorCode(error)
              }),
            ),
          ),
        ),
      ),
    ),
  );

  changeRole$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UserActions.changeRole),
      mergeMap(({ id, request }) =>
        this.userService.changeRole(id, request).pipe(
          map((user) => UserActions.changeRoleSuccess({ user })),
          catchError((error) =>
            of(
              UserActions.changeRoleFailure({
                error: extractErrorCode(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  setActive$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UserActions.setActive),
      mergeMap(({ id, active }) =>
        (active ? this.userService.activate(id) : this.userService.deactivate(id)).pipe(
          map((user) => UserActions.setActiveSuccess({ user })),
          catchError((error) =>
            of(
              UserActions.setActiveFailure({
                error: extractErrorCode(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );

  deleteUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UserActions.deleteUser),
      mergeMap(({ id }) =>
        this.userService.delete(id).pipe(
          map(() => UserActions.deleteUserSuccess({ id })),
          catchError((error) =>
            of(
              UserActions.deleteUserFailure({
                error: extractErrorCode(error),
              }),
            ),
          ),
        ),
      ),
    ),
  );
}
