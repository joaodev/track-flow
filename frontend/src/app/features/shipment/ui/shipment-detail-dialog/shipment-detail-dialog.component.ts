import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { TrackingProgressComponent } from '../tracking-progress/tracking-progress.component';
import { TrackingTimelineComponent } from '../tracking-timeline/tracking-timeline.component';
import { ShipmentActions } from '../../data-access/shipment.actions';
import { selectHistory } from '../../data-access/shipment.selectors';
import { Shipment } from '../../data-access/shipment.model';
import { TranslatePipe } from '../../../../core/translate.pipe';

export interface ShipmentDetailDialogData {
  shipment: Shipment;
}

@Component({
  selector: 'app-shipment-detail-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    TrackingProgressComponent,
    TrackingTimelineComponent,
    TranslatePipe,
  ],
  templateUrl: './shipment-detail-dialog.component.html',
  styleUrl: './shipment-detail-dialog.component.scss',
})
export class ShipmentDetailDialogComponent implements OnInit {
  data = inject<ShipmentDetailDialogData>(MAT_DIALOG_DATA);
  private store = inject(Store);

  history$ = this.store.select(selectHistory);

  ngOnInit(): void {
    this.store.dispatch(
      ShipmentActions.loadHistory({ trackingCode: this.data.shipment.trackingCode }),
    );
  }
}
