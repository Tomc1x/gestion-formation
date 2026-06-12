import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../services/auth.constants';

export const roleGuard = (allowedRoles: Role[]): CanActivateFn => () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return allowedRoles.includes(auth.currentRole()) ? true : router.createUrlTree(['/app/dashboard']);
};
