import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { MatButtonModule } from '@angular/material/button';
import { UserTableComponent } from '../../ui/user-table/user-table.component';
import { CreateUserFormComponent } from '../../ui/create-user-form/create-user-form.component';
import { UserActions } from '../../data-access/user.actions';
import { CreateUserRequest } from '../../data-access/user.model';
import { selectAllUsers, selectUsersError } from '../../data-access/user.selector';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, MatButtonModule, UserTableComponent, CreateUserFormComponent],
  templateUrl: './user-management.component.html',
})
export class UserManagementComponent implements OnInit {
  private store = inject(Store);

  users$ = this.store.select(selectAllUsers);
  error$ = this.store.select(selectUsersError);
  showCreateForm = signal(false);

  ngOnInit(): void {
    this.store.dispatch(UserActions.loadUsers());
  }

  toggleCreateForm(): void {
    this.showCreateForm.update((value) => !value);
  }

  onCreate(request: CreateUserRequest): void {
    this.store.dispatch(UserActions.createUser({ request }));
    this.showCreateForm.set(false);
  }

  onChangeRole(event: { id: number; role: string }): void {
    this.store.dispatch(UserActions.changeRole({ id: event.id, request: { role: event.role } }));
  }

  onToggleActive(event: { id: number; active: boolean }): void {
    this.store.dispatch(UserActions.setActive(event));
  }
}
