import { Observable } from "rxjs";
import {
  UserAdmin,
  CreateUserRequest,
  UpdateProfileRequest,
  ChangeRoleRequest,
  ChangePasswordRequest,
  InviteRequest,
} from "../models/user.model";

export abstract class BaseUserAdminAdapter {
  abstract getAll(): Observable<UserAdmin[]>;

  abstract create(body: CreateUserRequest): Observable<UserAdmin>;

  abstract update(uid: number, body: UpdateProfileRequest): Observable<UserAdmin>;

  abstract enable(uid: number): Observable<UserAdmin>;

  abstract disable(uid: number): Observable<UserAdmin>;

  abstract changeRole(uid: number, body: ChangeRoleRequest): Observable<UserAdmin>;

  abstract changePassword(uid: number, body: ChangePasswordRequest): Observable<UserAdmin>;

  abstract delete(uid: number): Observable<void>;

  abstract invite(body: InviteRequest): Observable<string>;
}
