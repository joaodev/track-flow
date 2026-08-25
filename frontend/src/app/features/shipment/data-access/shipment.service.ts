import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateShipmentRequest,
  Shipment,
  TrackingEvent,
  UpdateShipmentStatusRequest,
} from './shipment.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ShipmentService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/shipments`;

  getAll(): Observable<Shipment[]> {
    return this.http.get<Shipment[]>(this.baseUrl);
  }

  getByTrackingCode(trackingCode: string): Observable<Shipment> {
    return this.http.get<Shipment>(`${this.baseUrl}/${trackingCode}`);
  }

  getHistory(trackingCode: string): Observable<TrackingEvent[]> {
    return this.http.get<TrackingEvent[]>(`${this.baseUrl}/${trackingCode}/history`);
  }

  create(request: CreateShipmentRequest): Observable<Shipment> {
    return this.http.post<Shipment>(this.baseUrl, request);
  }

  updateStatus(trackingCode: string, request: UpdateShipmentStatusRequest): Observable<Shipment> {
    return this.http.put<Shipment>(`${this.baseUrl}/${trackingCode}/status`, request);
  }
}
