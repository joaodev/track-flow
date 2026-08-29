import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { CreateShipmentRequest } from '../../data-access/shipment.model';
import { TranslatePipe } from '../../../../core/translate.pipe';

@Component({
  selector: 'app-create-shipment-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    TranslatePipe,
  ],
  templateUrl: './create-shipment-form.component.html',
  styleUrl: './create-shipment-form.component.scss',
})
export class CreateShipmentFormComponent {
  private dialogRef = inject(MatDialogRef<CreateShipmentFormComponent>);
  private fb = inject(FormBuilder);

  form = this.fb.nonNullable.group({
    origin: ['', Validators.required],
    destination: ['', Validators.required],
    carrier: ['', Validators.required],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.form.getRawValue() as CreateShipmentRequest);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
