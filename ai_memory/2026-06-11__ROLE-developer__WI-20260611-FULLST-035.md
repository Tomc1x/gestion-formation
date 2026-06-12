# WI-20260611-FULLST-035 — Refonte flux auth frontend (expiration JWT + guestGuard login)

## Work Item
WI-20260611-FULLST-035

## Role
developer

## Status
DONE

## Scope
- `AuthService` : `isAuthenticated` doit refleter l'expiration du JWT (pas seulement sa presence).
- Nouveau `guestGuard` : utilisateur deja authentifie sur `/login` -> redirige vers `/app`.
- Appliquer `guestGuard` sur la route `login` ; `register` laisse inchange.
- Conserver l'intercepteur 401 existant (filet de securite reactif).

## Files Touched
- frontend/src/app/core/services/auth.service.ts
- frontend/src/app/core/guards/guest.guard.ts (nouveau)
- frontend/src/app/app.routes.ts

## Evidence
- `npx ng build` (frontend) : PASS, "Application bundle generation complete". Memes 4 warnings de budget CSS preexistants (utilisateurs/cursus/promotions/register .scss), aucun nouveau warning ni erreur de compilation TS.

## Decisions
1. `AuthService._isAuthenticated` est maintenant initialise via `hasValidToken()` (token present ET non expire), au lieu de `!!localStorage.getItem(TOKEN_KEY)`.
2. Ajout de deux methodes privees :
   - `hasValidToken(): boolean` -> `!!token && !isTokenExpired(token)`.
   - `isTokenExpired(token: string): boolean` -> decode le payload JWT (2e segment, base64url avec remplacement `-`/`_` -> `+`/`/`), lit `exp` (secondes epoch), compare a `Date.now()`. Si `exp` absent -> considere non expire (false) pour eviter de casser des tokens sans claim `exp`. Si decodage echoue (token malformé) -> considere expire (true), ce qui est le comportement sur d'autres cas similaires (fail-closed).
3. `login()` n'a pas ete modifie au-dela de ce qui existait (PIT-025 deja resolu par WI-032, signals `_isAuthenticated`/`_currentRole`/`_currentUser` deja set explicitement dans `tap()`) — relu avant modification, aucune regression introduite. Le token frais stocke est par construction non expire, donc `_isAuthenticated.set(true)` reste correct.
4. `guestGuard` (nouveau fichier `core/guards/guest.guard.ts`) suit exactement le pattern de `authGuard` (CanActivateFn, inject AuthService+Router, `router.createUrlTree([...])`). Si `auth.isAuthenticated()` est true -> `createUrlTree(['/app'])`, sinon `true`.
5. Route `login` : ajout `canActivate: [guestGuard]`. Route `register` : laissee inchangee (le WI laissait le choix ; un utilisateur authentifie qui navigue vers `/register` n'est pas un cas signale comme probleme, et `register` peut avoir un usage legitime — ex. referente_administrative invitant un nouvel utilisateur depuis une page deja authentifiee — donc pas de guard ajoute pour eviter de casser ce flux potentiel).
6. Intercepteur 401 (`auth.interceptor.ts`) non modifie, conserve comme filet de securite reactif tel que demande.

## Open Blockers
- Tests manuels (login normal -> /app, navigation /login authentifie -> /app, token expire simule -> authGuard redirige /login) NON executes dans cette session (pas d'environnement `ng serve` lance). `ng build` confirme la compilation; la logique a ete relue attentivement (decodage JWT standard, base64url, comparaison epoch ms vs exp en secondes).

## Next Actions
- Test manuel recommande en suivi : `ng serve`, login normal, puis naviguer vers `/login` (doit rediriger vers `/app`), puis dans la console du navigateur `localStorage.setItem('auth_token', '<jwt avec exp passe>')` et `localStorage.setItem('user', '{...}')`, recharger une route `/app/...` (doit rediriger vers `/login` via `authGuard` car `isAuthenticated()` retourne false).

## Recall Hints
- "AuthService isTokenExpired", "guestGuard /login redirige /app", "JWT exp decode AuthService", "PIT-025 _currentRole login"

## Proposed Rules
- TYPE: CONVENTION
  Title: JWT expiration check pattern in AuthService
  Scope: frontend/src/app/core/services/auth.service.ts, any future guard/service needing token validity
  Rule: Token validity (`isAuthenticated`) must always be derived from `hasValidToken()` (presence + `isTokenExpired()` via decoded `exp` claim), never from mere presence of the token key in localStorage.
  Why: A stale/expired token left in localStorage previously made `_isAuthenticated` permanently `true` until a 401 occurred reactively; this fixes the proactive check (WI-20260611-FULLST-035) needed for `guestGuard` and route guards to behave correctly on page load/navigation.
  How to apply: reuse `AuthService.isAuthenticated` (signal) for any "is the user currently logged in" check; do not re-read localStorage directly elsewhere.
  Evidence: frontend/src/app/core/services/auth.service.ts (`hasValidToken`, `isTokenExpired`), frontend/src/app/core/guards/guest.guard.ts
