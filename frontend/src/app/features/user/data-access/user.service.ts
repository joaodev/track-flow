import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';
import { CreateUserRequest, UpdateRoleRequest, User } from './user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/users`;

  getAll(): Observable<User[]> {
    return this.http.get<User[]>(this.baseUrl);
  }

  create(request: CreateUserRequest): Observable<User> {
    return this.http.post<User>(this.baseUrl, request);
  }

  changeRole(id: number, request: UpdateRoleRequest): Observable<User> {
    return this.http.patch<User>(`${this.baseUrl}/${id}/role`, request);
  }

  deactivate(id: number): Observable<User> {
    return this.http.patch<User>(`${this.baseUrl}/${id}/deactivate`, {});
  }

  activate(id: number): Observable<User> {
    return this.http.patch<User>(`${this.baseUrl}/${id}/activate`, {});
  }
}
