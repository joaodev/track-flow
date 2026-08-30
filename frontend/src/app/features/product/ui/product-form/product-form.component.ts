import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import {
  CreateProductRequest,
  Product,
  UpdateProductRequest,
} from '../../data-access/product.model';
import { TranslatePipe } from '../../../../core/translate.pipe';

export interface ProductFormDialogData {
  product?: Product;
}

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    TranslatePipe,
  ],
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.scss',
})
export class ProductFormComponent {
  private data = inject<ProductFormDialogData>(MAT_DIALOG_DATA, { optional: true }) ?? {};
  private dialogRef = inject(MatDialogRef<ProductFormComponent>);
  private fb = inject(FormBuilder);

  isEditMode = !!this.data.product;

  form = this.fb.nonNullable.group({
    sku: [{ value: this.data.product?.sku ?? '', disabled: this.isEditMode }, Validators.required],
    name: [this.data.product?.name ?? '', Validators.required],
    description: [this.data.product?.description ?? ''],
    unitPrice: [this.data.product?.unitPrice ?? 0, [Validators.required, Validators.min(0.01)]],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();

    if (this.isEditMode) {
      const request: UpdateProductRequest = {
        name: value.name,
        description: value.description || undefined,
        unitPrice: value.unitPrice,
      };
      this.dialogRef.close(request);
    } else {
      const request: CreateProductRequest = {
        sku: value.sku,
        name: value.name,
        description: value.description || undefined,
        unitPrice: value.unitPrice,
      };
      this.dialogRef.close(request);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
