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
import { Carrier } from '../../data-access/carrier.model';
import { TranslatePipe } from '../../../../core/translate.pipe';

@Component({
  selector: 'app-carrier-table',
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
  templateUrl: './carrier-table.component.html',
  styleUrl: './carrier-table.component.scss',
})
export class CarrierTableComponent implements OnChanges, AfterViewInit {
  @Input({ required: true }) carriers: Carrier[] = [];
  @Input({ required: true }) isAdmin = false;
  @Output() editCarrier = new EventEmitter<Carrier>();
  @Output() toggleActive = new EventEmitter<{ id: number; active: boolean }>();
  @Output() deleteCarrier = new EventEmitter<Carrier>();

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = ['name', 'contactInfo', 'active', 'actions'];
  dataSource = new MatTableDataSource<Carrier>([]);

  private viewReady = false;

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.viewReady = true;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['carriers']) return;
    this.dataSource.data = this.carriers;
    if (this.viewReady) {
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
    }
  }

  applyFilter(value: string): void {
    this.dataSource.filter = value.trim().toLowerCase();
  }
}
