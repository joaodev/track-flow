import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';
import { AdjustStockRequest, Inventory, UpdateThresholdRequest } from './inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/inventory`;

  getAll(): Observable<Inventory[]> {
    return this.http.get<Inventory[]>(this.baseUrl);
  }

  adjustStock(productId: number, request: AdjustStockRequest): Observable<Inventory> {
    return this.http.patch<Inventory>(`${this.baseUrl}/${productId}/adjust`, request);
  }

  updateThreshold(productId: number, request: UpdateThresholdRequest): Observable<Inventory> {
    return this.http.patch<Inventory>(`${this.baseUrl}/${productId}/threshold`, request);
  }
}
