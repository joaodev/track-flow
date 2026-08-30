import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Product } from '../../../product/data-access/product.model';
import { AdjustStockRequest, Inventory } from '../../data-access/inventory.model';
import { TranslatePipe } from '../../../../core/translate.pipe';

export interface StockAdjustDialogData {
  product: Product;
  inventory: Inventory;
}

@Component({
  selector: 'app-stock-adjust-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    TranslatePipe,
  ],
  templateUrl: './stock-adjust-form.component.html',
  styleUrl: './stock-adjust-form.component.scss',
})
export class StockAdjustFormComponent {
  data = inject<StockAdjustDialogData>(MAT_DIALOG_DATA);
  private dialogRef = inject(MatDialogRef<StockAdjustFormComponent>);
  private fb = inject(FormBuilder);

  form = this.fb.nonNullable.group({
    quantityDelta: [0, Validators.required],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request: AdjustStockRequest = { quantityDelta: this.form.getRawValue().quantityDelta };
    this.dialogRef.close(request);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
