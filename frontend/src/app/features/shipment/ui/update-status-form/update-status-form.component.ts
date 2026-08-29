import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { UpdateShipmentStatusRequest } from '../../data-access/shipment.model';
import { TranslatePipe } from '../../../../core/translate.pipe';

export interface UpdateStatusDialogData {
  trackingCode: string;
  currentStatus: string;
}

const STATUS_OPTIONS = ['CREATED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'];

@Component({
  selector: 'app-update-status-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatDialogModule,
    TranslatePipe,
  ],
  templateUrl: './update-status-form.component.html',
})
export class UpdateStatusFormComponent {
  data = inject<UpdateStatusDialogData>(MAT_DIALOG_DATA);
  private dialogRef = inject(MatDialogRef<UpdateStatusFormComponent>);
  private fb = inject(FormBuilder);

  statusOptions = STATUS_OPTIONS;

  form = this.fb.nonNullable.group({
    status: [this.data.currentStatus, Validators.required],
    location: [''],
    description: [''],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: UpdateShipmentStatusRequest = {
      status: value.status,
      location: value.location || undefined,
      description: value.description || undefined,
    };
    this.dialogRef.close(request);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
