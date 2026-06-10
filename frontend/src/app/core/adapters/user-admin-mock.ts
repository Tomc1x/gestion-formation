import { Observable, of } from "rxjs";
import { Injectable } from "@angular/core";
import {
  UserAdmin,
  CreateUserRequest,
  UpdateProfileRequest,
  ChangeRoleRequest,
  ChangePasswordRequest,
  InviteRequest,
} from "../models/user.model";
import { BaseUserAdminAdapter } from "./user-admin.adapter";

let MOCK_USERS: UserAdmin[] = [
  { uid: 1, firstName: 'Alice', lastName: 'Martin', email: 'alice.martin@eni.fr', role: 'ADMINISTRATEUR', enabled: true },
  { uid: 2, firstName: 'Bruno', lastName: 'Dupont', email: 'bruno.dupont@eni.fr', role: 'REFERENTE_ADMINISTRATIVE', enabled: true },
  { uid: 3, firstName: 'Claire', lastName: 'Lefevre', email: 'claire.lefevre@eni.fr', role: 'FORMATEUR', enabled: true },
  { uid: 4, firstName: 'David', lastName: 'Bernard', email: 'david.bernard@eni.fr', role: 'ETUDIANT', enabled: false },
];

@Injectable({ providedIn: 'root' })
export class MockUserAdminAdapter extends BaseUserAdminAdapter {
  getAll(): Observable<UserAdmin[]> {
    return of(MOCK_USERS);
  }

  create(body: CreateUserRequest): Observable<UserAdmin> {
    const created: UserAdmin = {
      uid: Math.max(...MOCK_USERS.map(u => u.uid), 0) + 1,
      firstName: body.firstName,
      lastName: body.lastName,
      email: body.email,
      role: body.role,
      enabled: true,
    };
    MOCK_USERS = [...MOCK_USERS, created];
    return of(created);
  }

  update(uid: number, body: UpdateProfileRequest): Observable<UserAdmin> {
    const updated = { ...this.findUser(uid), ...body };
    this.replaceUser(updated);
    return of(updated);
  }

  enable(uid: number): Observable<UserAdmin> {
    const updated = { ...this.findUser(uid), enabled: true };
    this.replaceUser(updated);
    return of(updated);
  }

  disable(uid: number): Observable<UserAdmin> {
    const updated = { ...this.findUser(uid), enabled: false };
    this.replaceUser(updated);
    return of(updated);
  }

  changeRole(uid: number, body: ChangeRoleRequest): Observable<UserAdmin> {
    const updated = { ...this.findUser(uid), role: body.role };
    this.replaceUser(updated);
    return of(updated);
  }

  changePassword(uid: number, _body: ChangePasswordRequest): Observable<UserAdmin> {
    return of(this.findUser(uid));
  }

  delete(uid: number): Observable<void> {
    MOCK_USERS = MOCK_USERS.filter(u => u.uid !== uid);
    return of(undefined);
  }

  invite(body: InviteRequest): Observable<string> {
    return of(`http://localhost/invite/${encodeURIComponent(body.email)}`);
  }

  private findUser(uid: number): UserAdmin {
    const user = MOCK_USERS.find(u => u.uid === uid);
    if (!user) {
      throw new Error(`User ${uid} not found`);
    }
    return user;
  }

  private replaceUser(updated: UserAdmin): void {
    MOCK_USERS = MOCK_USERS.map(u => (u.uid === updated.uid ? updated : u));
  }
}
