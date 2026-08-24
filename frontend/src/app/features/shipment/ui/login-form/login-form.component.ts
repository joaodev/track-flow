import { Component, EventEmitter, inject, Output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { LoginRequest } from '../../../auth/data-access/auth.model';

@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './login-form.component.html',
})
export class LoginFormComponent {
  @Output() login = new EventEmitter<LoginRequest>();

  private fb = inject(FormBuilder);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAsTouched();
      return;
    }
    this.login.emit(this.form.getRawValue());
  }
}
