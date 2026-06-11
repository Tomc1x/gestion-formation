import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable, tap } from 'rxjs';
import { Role, ROLE_META } from './auth.constants';

class AuthResponse {
  token: string | undefined;
  role: Role | undefined
  firstName: string | undefined;
  lastName: string | undefined
  email: string | undefined;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';

  private readonly _isAuthenticated = signal(!!localStorage.getItem(this.TOKEN_KEY));
  readonly isAuthenticated = this._isAuthenticated.asReadonly();

  private readonly _currentRole = signal<Role>('REF');
  readonly currentRole = this._currentRole.asReadonly();
  readonly currentRoleMeta = computed(() => ROLE_META[this._currentRole()]);

  private readonly _currentUser = signal<{ role:string; firstName: string; lastName: string; email: string } | null>(null);
  readonly currentUser = this._currentUser.asReadonly();
  private http = inject(HttpClient);

  login(credentials: { email: string; password: string }): Observable<boolean> {
    this.logout();
    return this.http.post<AuthResponse>('/api/auth/login', credentials).pipe(
      tap(response => {
        if (response.token != null) {
          localStorage.setItem(this.TOKEN_KEY, response.token);
        }
        localStorage.setItem('user', JSON.stringify({
          role: response.role,
          firstName: response.firstName,
          lastName: response.lastName,
          email: response.email
        }));
        this._isAuthenticated.set(true);
        this._currentUser.set({
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
    this._isAuthenticated.set(false);
    this._currentRole.set('REF');
    this._currentUser.set(null);
  }
}
