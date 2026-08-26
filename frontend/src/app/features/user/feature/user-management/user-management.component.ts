import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { UserTableComponent } from '../../ui/user-table/user-table.component';
import { CreateUserFormComponent } from '../../ui/create-user-form/create-user-form.component';
import { UserActions } from '../../data-access/user.actions';
import { CreateUserRequest } from '../../data-access/user.model';
import { selectAllUsers, selectUsersError } from '../../data-access/user.selector';
import { ConfirmDialogComponent } from '../../../../shared/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, MatButtonModule, UserTableComponent],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss',
})
export class UserManagementComponent implements OnInit {
  private store = inject(Store);
  private dialog = inject(MatDialog);

  users$ = this.store.select(selectAllUsers);
  error$ = this.store.select(selectUsersError);

  ngOnInit(): void {
    this.store.dispatch(UserActions.loadUsers());
  }

  onOpenCreateForm(): void {
    const dialogRef = this.dialog.open(CreateUserFormComponent, { width: '420px' });

    dialogRef.afterClosed().subscribe((request: CreateUserRequest | undefined) => {
      if (request) {
        this.store.dispatch(UserActions.createUser({ request }));
      }
    });
  }

  onChangeRole(event: { id: number; role: string }): void {
    this.store.dispatch(UserActions.changeRole({ id: event.id, request: { role: event.role } }));
  }

  onToggleActive(event: { id: number; active: boolean }): void {
    this.store.dispatch(UserActions.setActive(event));
  }

  onDeleteUser(id: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete user',
        message: 'Are you sure you want to delete this user? This cannot be undone.',
      },
      width: '400px',
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean | undefined) => {
      if (confirmed) {
        this.store.dispatch(UserActions.deleteUser({ id }));
      }
    });
  }
}
