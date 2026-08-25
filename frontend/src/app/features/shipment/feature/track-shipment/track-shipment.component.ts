import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { TrackSearchFormComponent } from '../../ui/track-search-form/track-search-form.component';
import { TrackingTimelineComponent } from '../../ui/tracking-timeline/tracking-timeline.component';
import { selectError, selectHistory, selectLoading, selectShipmentByTrackingCode } from '../../data-access/shipment.selectors';
import { Observable } from 'rxjs';
import { Shipment, TrackingEvent } from '../../data-access/shipment.model';
import { ShipmentActions } from '../../data-access/shipment.actions';

@Component({
  selector: 'app-track-shipment',
  standalone: true,
  imports: [CommonModule, TrackSearchFormComponent, TrackingTimelineComponent],
  templateUrl: './track-shipment.component.html',
})
export class TrackShipmentComponent {
  private store = inject(Store);

  loading$: Observable<boolean> = this.store.select(selectLoading);
  error$: Observable<string | null> = this.store.select(selectError);
  history$: Observable<TrackingEvent[]> = this.store.select(selectHistory);
  shipment$: Observable<Shipment | undefined> | null = null;

  onSearch(trackingCode: string): void {
    this.shipment$ = this.store.select(selectShipmentByTrackingCode(trackingCode));
    this.store.dispatch(ShipmentActions.loadShipment({ trackingCode }));
    this.store.dispatch(ShipmentActions.loadHistory({ trackingCode }));
  }
}
