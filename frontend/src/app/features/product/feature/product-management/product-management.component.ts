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
import { TranslationService } from '../../../../core/translation.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  selectInventoryByProductId,
  selectLastAlert,
} from '../../../inventory/data-access/inventory.selectors';
import { InventoryActions } from '../../../inventory/data-access/inventory.actions';
import { asyncScheduler, filter, observeOn } from 'rxjs';
import { StockAdjustFormComponent } from '../../../inventory/ui/stock-adjust-form/stock-adjust-form.component';
import { AdjustStockRequest, LowStockEvent } from '../../../inventory/data-access/inventory.model';
import { selectIsAdmin } from '../../../auth/data-access/auth.selectors';
import { ConfirmDialogComponent } from '../../../../shared/confirm-dialog/confirm-dialog.component';

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
  private snackBar = inject(MatSnackBar);
  private translation = inject(TranslationService);

  products$ = this.store.select(selectAllProducts);
  error$ = this.store.select(selectError);
  inventoryByProductId$ = this.store.select(selectInventoryByProductId);
  isAdmin$ = this.store.select(selectIsAdmin);

  ngOnInit(): void {
    this.store.dispatch(ProductActions.loadProducts());
    this.store.dispatch(InventoryActions.loadInventory());

    this.store
      .select(selectLastAlert)
      .pipe(
        filter((alert): alert is LowStockEvent => !!alert),
        observeOn(asyncScheduler),
      )
      .subscribe((alert) => {
        this.snackBar.open(
          this.translation.t('inventory.lowStockToast', {
            productId: alert.productId,
            available: alert.availableQuantity,
          }),
          undefined,
          { duration: 5000 },
        );
      });
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

  onAdjustStock(
    product: Product,
    inventoryByProductId: ReturnType<typeof selectInventoryByProductId>,
  ): void {
    const inventory = inventoryByProductId[product.id];
    if (!inventory) {
      this.snackBar.open(this.translation.t('errors.INVENTORY_NOT_FOUND'), undefined, {
        duration: 4000,
      });
      return;
    }

    const dialogRef = this.dialog.open(StockAdjustFormComponent, {
      data: { product, inventory },
      width: '400px',
    });

    dialogRef.afterClosed().subscribe((request: AdjustStockRequest | undefined) => {
      if (request) {
        this.store.dispatch(InventoryActions.adjustStock({ productId: product.id, request }));
      }
    });
  }

  onDeleteProduct(product: Product): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.translation.t('product.deleteConfirm.title'),
        message: this.translation.t('product.deleteConfirm.message', { name: product.name }),
      },
      width: '400px',
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean | undefined) => {
      if (confirmed) {
        this.store.dispatch(ProductActions.deleteProduct({ id: product.id }));
      }
    });
  }
}
