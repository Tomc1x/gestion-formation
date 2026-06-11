# WI-20260610-BACKEN-011 — Role guard for admin routes

## Work Item
WI-20260610-BACKEN-011

## Role
developer

## Status
DONE

## Scope
Frontend only — created reusable role-based route guard and applied it to `admin/*` routes.

## Files Touched
- `frontend/src/app/core/guards/role.guard.ts` (new)
- `frontend/src/app/app.routes.ts` (import + `canActivate` on 3 admin routes)

## Evidence
- `npx tsc --noEmit -p tsconfig.app.json` → exit 0, no output.
- `npx ng build` → succeeded, only pre-existing SCSS budget warnings (register, utilisateurs), unrelated to this change.

## Decisions
- Implemented `roleGuard` as a factory returning a `CanActivateFn`, matching the requested signature `roleGuard(allowedRoles: Role[]): CanActivateFn`. Style copied exactly from `auth.guard.ts` (functional, `inject()`, no class, no comments).
- Redirect target on denial: `router.createUrlTree(['/app/dashboard'])`, as specified.
- Parent route `app` keeps `authGuard` unchanged; each admin child route gets its own `canActivate: [roleGuard([...])]`.

## Open Blockers
None.

## Next Actions
None — scope complete.

## Recall Hints
To protect a future route by role, import `roleGuard` from `./core/guards/role.guard` in `app.routes.ts` and add `canActivate: [roleGuard(['ADMIN'])]` (or any combination of `'REF' | 'ADMIN' | 'FORMATEUR' | 'ELEVE'`) to the route object, e.g.:

```ts
{
  title: 'Gestion des sessions',
  path: 'admin/sessions',
  canActivate: [roleGuard(['ADMIN', 'REF'])],
  loadComponent: () => import('./features/administration/sessions/sessions').then(m => m.SessionsComponent)
}
```

If the user's `currentRole()` is not in the allowed list, they are redirected to `/app/dashboard`.

## Proposed Rules
None — pattern is a direct extension of the existing `auth.guard.ts` convention, already self-evident from the code.
