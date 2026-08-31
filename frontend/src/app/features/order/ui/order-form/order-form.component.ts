import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { selectActiveProducts } from '../../../product/data-access/product.selectors';
import { selectActiveCustomers } from '../../../customer/data-access/customer.selectors';
import { CreateOrderRequest } from '../../data-access/order.model';
import { TranslatePipe } from '../../../../core/translate.pipe';

@Component({
  selector: 'app-order-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    TranslatePipe,
  ],
  templateUrl: './order-form.component.html',
  styleUrl: './order-form.component.scss',
})
export class OrderFormComponent {
  private dialogRef = inject(MatDialogRef<OrderFormComponent>);
  private fb = inject(FormBuilder);
  private store = inject(Store);

  products$ = this.store.select(selectActiveProducts);
  customers$ = this.store.select(selectActiveCustomers);

  form = this.fb.nonNullable.group({
    customerId: [null as number | null, Validators.required],
    origin: ['', Validators.required],
    destination: ['', Validators.required],
    items: this.fb.array([this.createItemGroup()]),
  });

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  private createItemGroup() {
    return this.fb.nonNullable.group({
      productId: [null as number | null, Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
    });
  }

  addItem(): void {
    this.items.push(this.createItemGroup());
  }

  removeItem(index: number): void {
    if (this.items.length > 1) {
      this.items.removeAt(index);
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: CreateOrderRequest = {
      customerId: value.customerId!,
      origin: value.origin,
      destination: value.destination,
      items: value.items.map((item) => ({ productId: item.productId!, quantity: item.quantity })),
    };
    this.dialogRef.close(request);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
