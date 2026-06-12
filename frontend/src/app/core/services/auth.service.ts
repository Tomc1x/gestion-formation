import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable, tap } from 'rxjs';
import { Role, ROLE_META } from './auth.constants';
import { BACKEND_TO_FRONTEND_ROLE, BackendRole } from '../models/user.model';

class AuthResponse {
  token: string | undefined;
  uid: number | undefined;
  role: Role | undefined
  firstName: string | undefined;
  lastName: string | undefined
  email: string | undefined;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'user';

  private readonly _isAuthenticated = signal(!!localStorage.getItem(this.TOKEN_KEY));
  readonly isAuthenticated = this._isAuthenticated.asReadonly();

  private readonly storedUser = this.readStoredUser();

  private readonly _currentRole = signal<Role>(
    this.storedUser ? BACKEND_TO_FRONTEND_ROLE[this.storedUser.role as BackendRole] ?? 'REF' : 'REF'
  );
  readonly currentRole = this._currentRole.asReadonly();
  readonly currentRoleMeta = computed(() => ROLE_META[this._currentRole()]);

  private readonly _currentUser = signal<{ uid: number | null; role:string; firstName: string; lastName: string; email: string } | null>(this.storedUser);
  readonly currentUser = this._currentUser.asReadonly();
  readonly currentUserId = computed(() => this._currentUser()?.uid ?? null);
  private http = inject(HttpClient);

  private readStoredUser(): { uid: number | null; role: string; firstName: string; lastName: string; email: string } | null {
    if (!localStorage.getItem(this.TOKEN_KEY)) {
      return null;
    }
    const raw = localStorage.getItem(this.USER_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }

  login(credentials: { email: string; password: string }): Observable<boolean> {
    this.logout();
    return this.http.post<AuthResponse>('/api/auth/login', credentials).pipe(
      tap(response => {
        if (response.token != null) {
          localStorage.setItem(this.TOKEN_KEY, response.token);
        }
        localStorage.setItem(this.USER_KEY, JSON.stringify({
          uid: response.uid ?? null,
          role: response.role,
          firstName: response.firstName,
          lastName: response.lastName,
          email: response.email
        }));
        this._isAuthenticated.set(true);
        this._currentUser.set({
          uid:response.uid ?? null,
          role: response.role ?? '',
          firstName: response.firstName ?? '',
          lastName: response.lastName ?? '',
          email: response.email ?? ''
        });
      }),
      map(() => true)
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this._isAuthenticated.set(false);
    this._currentRole.set('REF');
    this._currentUser.set(null);
  }
}
