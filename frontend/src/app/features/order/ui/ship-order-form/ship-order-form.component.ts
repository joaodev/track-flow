import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { selectActiveCarriers } from '../../../carrier/data-access/carrier.selectors';
import { ShipOrderRequest } from '../../data-access/order.model';
import { TranslatePipe } from '../../../../core/translate.pipe';

@Component({
  selector: 'app-ship-order-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
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
  private store = inject(Store);

  carriers$ = this.store.select(selectActiveCarriers);

  form = this.fb.nonNullable.group({
    carrierId: [null as number | null, Validators.required],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close({ carrierId: this.form.getRawValue().carrierId! } as ShipOrderRequest);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
