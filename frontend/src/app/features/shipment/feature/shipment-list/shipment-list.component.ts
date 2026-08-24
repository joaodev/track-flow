import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { MatButtonModule } from '@angular/material/button';
import { ShipmentTableComponent } from '../../ui/shipment-table/shipment-table.component';
import { CreateShipmentFormComponent } from '../../ui/create-shipment-form/create-shipment-form.component';
import { ShipmentActions } from '../../data-access/shipment.actions';
import {
  selectAllShipments,
  selectError,
  selectLoading,
} from '../../data-access/shipment.selectors';
import { CreateShipmentRequest } from '../../data-access/shipment.model';

@Component({
  selector: 'app-shipment-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, ShipmentTableComponent, CreateShipmentFormComponent],
  templateUrl: './shipment-list.component.html',
})
export class ShipmentListComponent implements OnInit {
  private store = inject(Store);

  shipments$ = this.store.select(selectAllShipments);
  loading$ = this.store.select(selectLoading);
  error$ = this.store.select(selectError);

  showCreateForm = signal(false);

  ngOnInit(): void {
    this.store.dispatch(ShipmentActions.loadShipments());
  }

  toggleCreateForm(): void {
    this.showCreateForm.update((value) => !value);
  }

  onCreate(request: CreateShipmentRequest): void {
    this.store.dispatch(ShipmentActions.createShipment({ request }));
    this.showCreateForm.set(false);
  }
}
