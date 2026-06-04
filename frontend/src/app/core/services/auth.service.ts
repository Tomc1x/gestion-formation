import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, tap, map, catchError } from 'rxjs';

// interface AuthResponse {
//   token: string;
// }

@Injectable({ providedIn: 'root' })
export class AuthService {
  // private readonly http = inject(HttpClient);
  private readonly TOKEN_KEY = 'auth_token';

  private readonly _isAuthenticated = signal(!!localStorage.getItem(this.TOKEN_KEY));
  readonly isAuthenticated = this._isAuthenticated.asReadonly();

  // Implémentation réelle (à activer quand le backend est prêt) :
  // login(credentials: { username: string; password: string }): Observable<boolean> {
  //   return this.http.post<AuthResponse>('/api/auth/login', credentials).pipe(
  //     tap(response => {
  //       localStorage.setItem(this.TOKEN_KEY, response.token);
  //       this._isAuthenticated.set(true);
  //     }),
  //     map(() => true),
  //     catchError(() => of(false))
  //   );
  // }

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
