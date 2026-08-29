import { Component, EventEmitter, inject, Output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TranslatePipe } from '../../../../core/translate.pipe';
import { LoginRequest } from '../../data-access/auth.model';

@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    TranslatePipe,
  ],
  templateUrl: './login-form.component.html',
  styleUrl: 'login-form.component.scss',
})
export class LoginFormComponent {
  @Output() login = new EventEmitter<LoginRequest>();

  private fb = inject(FormBuilder);

  hidePassword = true;

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  togglePasswordVisibility(): void {
    this.hidePassword = !this.hidePassword;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAsTouched();
      return;
    }
    this.login.emit(this.form.getRawValue());
  }
}
