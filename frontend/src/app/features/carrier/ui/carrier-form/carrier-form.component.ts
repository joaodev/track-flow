import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Carrier, CarrierRequest } from '../../data-access/carrier.model';
import { TranslatePipe } from '../../../../core/translate.pipe';

export interface CarrierFormDialogData {
  carrier?: Carrier;
}

@Component({
  selector: 'app-carrier-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    TranslatePipe,
  ],
  templateUrl: './carrier-form.component.html',
  styleUrl: './carrier-form.component.scss',
})
export class CarrierFormComponent {
  private data = inject<CarrierFormDialogData>(MAT_DIALOG_DATA, { optional: true }) ?? {};
  private dialogRef = inject(MatDialogRef<CarrierFormComponent>);
  private fb = inject(FormBuilder);

  isEditMode = !!this.data.carrier;

  form = this.fb.nonNullable.group({
    name: [this.data.carrier?.name ?? '', Validators.required],
    contactInfo: [this.data.carrier?.contactInfo ?? ''],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: CarrierRequest = {
      name: value.name,
      contactInfo: value.contactInfo || undefined,
    };
    this.dialogRef.close(request);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
