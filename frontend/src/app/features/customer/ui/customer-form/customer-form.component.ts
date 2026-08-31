import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Customer, CustomerRequest } from '../../data-access/customer.model';
import { TranslatePipe } from '../../../../core/translate.pipe';

export interface CustomerFormDialogData {
  customer?: Customer;
}

@Component({
  selector: 'app-customer-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    TranslatePipe,
  ],
  templateUrl: './customer-form.component.html',
  styleUrl: './customer-form.component.scss',
})
export class CustomerFormComponent {
  private data = inject<CustomerFormDialogData>(MAT_DIALOG_DATA, { optional: true }) ?? {};
  private dialogRef = inject(MatDialogRef<CustomerFormComponent>);
  private fb = inject(FormBuilder);

  isEditMode = !!this.data.customer;

  form = this.fb.nonNullable.group({
    name: [this.data.customer?.name ?? '', Validators.required],
    email: [this.data.customer?.email ?? '', [Validators.required, Validators.email]],
    phone: [this.data.customer?.phone ?? '', Validators.required],
    address: [this.data.customer?.address ?? '', Validators.required],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.form.getRawValue() as CustomerRequest);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
