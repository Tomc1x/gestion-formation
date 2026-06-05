import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Role, ROLE_META, USER_FOR_ROLE } from './auth.constants';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';

  private readonly _isAuthenticated = signal(!!localStorage.getItem(this.TOKEN_KEY));
  readonly isAuthenticated = this._isAuthenticated.asReadonly();

  private readonly _currentRole = signal<Role>('REF');
  readonly currentRole = this._currentRole.asReadonly();
  readonly currentRoleMeta = computed(() => ROLE_META[this._currentRole()]);

  private readonly _currentUser = signal<{ username: string; firstName: string; lastName: string; email: string } | null>(null);
  readonly currentUser = this._currentUser.asReadonly();


  login(credentials: { username: string; password: string }): Observable<boolean> {
    localStorage.setItem(this.TOKEN_KEY, 'mock-token-dev');
    localStorage.setItem('user', JSON.stringify({ username: credentials.username, firstName: 'Jean', lastName: 'Dupont', email: `${credentials.username}@example.com` }));
    this._currentRole.set('ADMIN');
    this._currentUser.set({ username: credentials.username, firstName: 'Jean', lastName: 'Dupont', email: `${credentials.username}@example.com` });
    this._isAuthenticated.set(true);
    return of(true);
  }

  // (à activer quand le backend est prêt) :
  // login(credentials: { username: string; password: string }): Observable<boolean> {
  //   return this.http.post<AuthResponse>('/api/auth/login', credentials).pipe(
  //     tap(response => {
  //       localStorage.setItem(this.TOKEN_KEY, response.token);
  //       localStorage.setItem('user', JSON.stringify({ username: response.username, role: response.role, firstName: response.firstName, lastName: response.lastName, email: response.email }));
  //       this._isAuthenticated.set(true);
  //     }),
  //     map(() => true),
  //     catchError(() => of(false))
  //   );
  // }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this._isAuthenticated.set(false);
    this._currentRole.set('REF');
    this._currentUser.set(null);
  }
}
