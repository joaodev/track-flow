import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';
import { TranslatePipe } from '../../../../core/translate.pipe';
import { Product } from '../../data-access/product.model';
import { Dictionary } from '@ngrx/entity';
import { Inventory } from '../../../inventory/data-access/inventory.model';
import { LocalCurrencyPipe } from '../../../../core/currency/local-currency.pipe';

@Component({
  selector: 'app-product-table',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIcon,
    MatTooltip,
    TranslatePipe,
    LocalCurrencyPipe,
  ],
  templateUrl: './product-table.component.html',
  styleUrl: './product-table.component.scss',
})
export class ProductTableComponent implements OnChanges, AfterViewInit {
  @Input({ required: true }) products: Product[] = [];
  @Input({ required: true }) inventoryByProductId: Dictionary<Inventory> = {};
  @Output() editProduct = new EventEmitter<Product>();
  @Output() toggleActive = new EventEmitter<{ id: number; active: boolean }>();
  @Output() adjustStock = new EventEmitter<Product>();
  @Input({ required: true }) isAdmin = false;
  @Output() deleteProduct = new EventEmitter<Product>();

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = ['sku', 'name', 'unitPrice', 'stock', 'active', 'actions'];
  dataSource = new MatTableDataSource<Product>([]);

  private viewReady = false;

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.viewReady = true;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['products']) return;
    this.dataSource.data = this.products;
    if (this.viewReady) {
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
    }
  }

  applyFilter(value: string): void {
    this.dataSource.filter = value.trim().toLowerCase();
  }
}
