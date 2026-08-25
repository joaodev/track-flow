import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { User } from '../../data-access/user.model';

@Component({
  selector: 'app-user-table',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatSelectModule],
  templateUrl: './user-table.component.html',
})
export class UserTableComponent {
  @Input({ required: true }) users: User[] = [];
  @Output() changeRole = new EventEmitter<{ id: number; role: string }>();
  @Output() toggleActive = new EventEmitter<{ id: number; active: boolean }>();

  displayedColumns = ['email', 'role', 'active', 'createdAt', 'actions'];
  roleOptions = ['ADMIN', 'OPS'];
}
