import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { ShipOrderRequest } from '../../data-access/order.model';
import { TranslatePipe } from '../../../../core/translate.pipe';

@Component({
  selector: 'app-ship-order-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    TranslatePipe,
  ],
  templateUrl: './ship-order-form.component.html',
  styleUrl: './ship-order-form.component.scss',
})
export class ShipOrderFormComponent {
  private dialogRef = inject(MatDialogRef<ShipOrderFormComponent>);
  private fb = inject(FormBuilder);

  form = this.fb.nonNullable.group({
    carrier: ['', Validators.required],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close({ carrier: this.form.getRawValue().carrier } as ShipOrderRequest);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
