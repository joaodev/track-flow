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
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIcon } from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';
import { Order } from '../../data-access/order.model';
import { TranslatePipe } from '../../../../core/translate.pipe';
import { Dictionary } from '@ngrx/entity';
import { Customer } from '../../../customer/data-access/customer.model';

@Component({
  selector: 'app-order-table',
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
  ],
  templateUrl: './order-table.component.html',
  styleUrl: './order-table.component.scss',
})
export class OrderTableComponent implements OnChanges, AfterViewInit {
  @Input({ required: true }) orders: Order[] = [];
  @Output() viewDetails = new EventEmitter<Order>();
  @Output() confirmOrder = new EventEmitter<Order>();
  @Output() shipOrder = new EventEmitter<Order>();
  @Output() cancelOrder = new EventEmitter<Order>();
  @Input({ required: true }) customersById: Dictionary<Customer> = {};

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = [
    'orderNumber',
    'customerName',
    'destination',
    'status',
    'createdAt',
    'actions',
  ];
  dataSource = new MatTableDataSource<Order>([]);

  private viewReady = false;

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.viewReady = true;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['orders']) return;
    this.dataSource.data = this.orders;
    if (this.viewReady) {
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
    }
  }

  applyFilter(value: string): void {
    this.dataSource.filter = value.trim().toLowerCase();
  }
}
