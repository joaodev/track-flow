import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { ProductTableComponent } from '../../ui/product-table/product-table.component';
import { ProductFormComponent } from '../../ui/product-form/product-form.component';
import { ProductActions } from '../../data-access/product.actions';
import { selectAllProducts, selectError } from '../../data-access/product.selectors';
import {
  CreateProductRequest,
  Product,
  UpdateProductRequest,
} from '../../data-access/product.model';
import { TranslatePipe } from '../../../../core/translate.pipe';

@Component({
  selector: 'app-product-management',
  standalone: true,
  imports: [CommonModule, MatButtonModule, ProductTableComponent, TranslatePipe],
  templateUrl: './product-management.component.html',
  styleUrl: './product-management.component.scss',
})
export class ProductManagementComponent implements OnInit {
  private store = inject(Store);
  private dialog = inject(MatDialog);

  products$ = this.store.select(selectAllProducts);
  error$ = this.store.select(selectError);

  ngOnInit(): void {
    this.store.dispatch(ProductActions.loadProducts());
  }

  onOpenCreateForm(): void {
    const dialogRef = this.dialog.open(ProductFormComponent, { width: '420px' });

    dialogRef.afterClosed().subscribe((request: CreateProductRequest | undefined) => {
      if (request) {
        this.store.dispatch(ProductActions.createProduct({ request }));
      }
    });
  }

  onEditProduct(product: Product): void {
    const dialogRef = this.dialog.open(ProductFormComponent, { data: { product }, width: '420px' });

    dialogRef.afterClosed().subscribe((request: UpdateProductRequest | undefined) => {
      if (request) {
        this.store.dispatch(ProductActions.updateProduct({ id: product.id, request }));
      }
    });
  }

  onToggleActive(event: { id: number; active: boolean }): void {
    this.store.dispatch(ProductActions.setActive(event));
  }
}
