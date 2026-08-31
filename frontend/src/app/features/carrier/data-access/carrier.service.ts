import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Carrier, CarrierRequest } from './carrier.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CarrierService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/carriers`;

  getAll(): Observable<Carrier[]> {
    return this.http.get<Carrier[]>(this.baseUrl);
  }

  create(request: CarrierRequest): Observable<Carrier> {
    return this.http.post<Carrier>(this.baseUrl, request);
  }

  update(id: number, request: CarrierRequest): Observable<Carrier> {
    return this.http.put<Carrier>(`${this.baseUrl}/${id}`, request);
  }

  activate(id: number): Observable<Carrier> {
    return this.http.patch<Carrier>(`${this.baseUrl}/${id}/activate`, {});
  }

  deactivate(id: number): Observable<Carrier> {
    return this.http.patch<Carrier>(`${this.baseUrl}/${id}/deactivate`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
