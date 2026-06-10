import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import {
  UserAdmin,
  CreateUserRequest,
  UpdateProfileRequest,
  ChangeRoleRequest,
  ChangePasswordRequest,
  InviteRequest,
} from "../models/user.model";
import { BaseUserAdminAdapter } from "./user-admin.adapter";

@Injectable({ providedIn: 'root' })
export class HttpUserAdminAdapter extends BaseUserAdminAdapter {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/admin/users';

  getAll(): Observable<UserAdmin[]> {
    return this.http.get<UserAdmin[]>(this.API);
  }

  create(body: CreateUserRequest): Observable<UserAdmin> {
    return this.http.post<UserAdmin>(this.API, body);
  }

  update(uid: number, body: UpdateProfileRequest): Observable<UserAdmin> {
    return this.http.put<UserAdmin>(`${this.API}/${uid}`, body);
  }

  enable(uid: number): Observable<UserAdmin> {
    return this.http.put<UserAdmin>(`${this.API}/${uid}/enable`, {});
  }

  disable(uid: number): Observable<UserAdmin> {
    return this.http.put<UserAdmin>(`${this.API}/${uid}/disable`, {});
  }

  changeRole(uid: number, body: ChangeRoleRequest): Observable<UserAdmin> {
    return this.http.put<UserAdmin>(`${this.API}/${uid}/role`, body);
  }

  changePassword(uid: number, body: ChangePasswordRequest): Observable<UserAdmin> {
    return this.http.put<UserAdmin>(`${this.API}/${uid}/password`, body);
  }

  delete(uid: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${uid}`);
  }

  invite(body: InviteRequest): Observable<string> {
    return this.http.post('/api/admin/invite', body, { responseType: 'text' });
  }
}
