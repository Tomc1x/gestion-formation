import { Injectable, signal } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';

  private readonly _isAuthenticated = signal(!!localStorage.getItem(this.TOKEN_KEY));
  readonly isAuthenticated = this._isAuthenticated.asReadonly();

  // TODO: remplacer par this.http.post<AuthResponse>('/api/auth/login', credentials)
  login(credentials: { username: string; password: string }): Observable<boolean> {
    localStorage.setItem(this.TOKEN_KEY, 'mock-token-dev');
    this._isAuthenticated.set(true);
    return of(true);
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this._isAuthenticated.set(false);
  }
}
