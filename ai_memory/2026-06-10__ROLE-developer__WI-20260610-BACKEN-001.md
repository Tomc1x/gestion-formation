# WI-20260610-BACKEN-001 — Adapter pattern for User Admin (frontend)

## Work Item
WI-20260610-BACKEN-001

## Role
developer

## Status
DONE

## Scope
Replicate the `BaseCalendarAdapter` / `MockCalendarAdapter` adapter pattern for the
admin-user feature: create `BaseUserAdminAdapter`, `HttpUserAdminAdapter`,
`MockUserAdminAdapter`, wire `HttpUserAdminAdapter` as default in `app.config.ts`,
update `UtilisateursComponent` to inject the abstract adapter, and remove the now
unused `UserAdminService`.

## Files Touched
- `frontend/src/app/core/adapters/user-admin.adapter.ts` (new) — abstract class
  `BaseUserAdminAdapter` with the 9 abstract methods (getAll, create, update, enable,
  disable, changeRole, changePassword, delete, invite), same signatures/types as the
  former `UserAdminService`.
- `frontend/src/app/core/adapters/user-admin-http.adapter.ts` (new) — `@Injectable
  ({providedIn:'root'}) HttpUserAdminAdapter extends BaseUserAdminAdapter`, HTTP calls
  inlined directly (same `API = '/api/admin/users'` constant, same `/api/admin/invite`
  endpoint).
- `frontend/src/app/core/adapters/user-admin-mock.ts` (new) — `@Injectable
  ({providedIn:'root'}) MockUserAdminAdapter extends BaseUserAdminAdapter`, in-memory
  CRUD on a module-level `MOCK_USERS: UserAdmin[]` array (4 users, varied
  roles/enabled), all methods return `Observable` via `of(...)`, immutable updates
  (spread + array map/filter). `invite` returns `of('http://localhost/invite/...')`.
  This is a dev/test wiring adapter only (mirrors `MockCalendarAdapter`), not a
  demo-account/UI feature.
- `frontend/src/app/app.config.ts` — added import of `HttpUserAdminAdapter` /
  `BaseUserAdminAdapter` and provider
  `{provide: BaseUserAdminAdapter, useClass: HttpUserAdminAdapter}` (kept alongside
  the existing calendar provider).
- `frontend/src/app/features/administration/utilisateurs/utilisateurs.ts` — replaced
  `import { UserAdminService } from '../../../core/services/user-admin.service'` with
  `import { BaseUserAdminAdapter } from '../../../core/adapters/user-admin.adapter'`
  and `inject(UserAdminService)` → `inject(BaseUserAdminAdapter)`. No other change
  (field name `userAdminService` kept, all call sites unchanged since method names
  are identical).
- `frontend/src/app/core/services/user-admin.service.ts` — deleted (no remaining
  references after the change).

## Evidence
- `grep -rn "UserAdminService" src` → no matches (confirmed before deletion that only
  the service file itself and `utilisateurs.ts` referenced it; both handled).
- `npx tsc --noEmit -p tsconfig.app.json` → exit 0, no output (clean compile).
- `npx ng build` → TypeScript/Angular compilation succeeded; the only failure is a
  pre-existing CSS budget error on
  `src/app/features/administration/utilisateurs/utilisateurs.scss` (9.89 kB vs 8 kB
  error budget), introduced in commit `85a8ff8` (prior work item), untouched by this
  change. Verified via `git log -1 -- .../utilisateurs.scss` → last touched in
  `85a8ff8`, before this WI.

## Decisions
- Followed the calendar adapter style exactly: no constructor, `inject()` for
  dependencies, `@Injectable({providedIn: 'root'})` on both Http and Mock
  implementations, abstract class (not interface) as the injection token.
- `HttpUserAdminAdapter` inlines the HTTP calls directly (no delegation to the old
  service), per scope instructions.
- `MockUserAdminAdapter.changePassword` keeps the `body` parameter (renamed `_body`
  to satisfy "unused param" readability without using `any`/removing from the
  abstract signature) since the abstract signature requires it but the mock has
  nothing meaningful to mutate.
- Did not add `override` keywords — the existing `MockCalendarAdapter` doesn't use
  them either and `noImplicitOverride` does not appear to require it for abstract
  method implementations in this project's tsconfig; build/tsc confirmed this is
  fine.

## Open Blockers
None. The pre-existing SCSS budget error on `utilisateurs.scss` is out of scope for
this WI (no HTML/SCSS changes were made or requested) and should be tracked
separately if the team wants `ng build` to fully pass.

## Next Actions
- None for this WI. Optional follow-up (separate WI): fix the `utilisateurs.scss`
  budget overflow so `ng build` passes cleanly.

## Recall Hints
- Adapter pattern reference: `frontend/src/app/core/adapters/calendar.adapter.ts` /
  `calendar-mock.ts`.
- New user-admin adapters live in `frontend/src/app/core/adapters/`:
  `user-admin.adapter.ts` (abstract), `user-admin-http.adapter.ts` (HTTP impl),
  `user-admin-mock.ts` (mock impl).
- Default provider wiring is in `frontend/src/app/app.config.ts`.

## Proposed Rules
- TYPE: CONVENTION
  Title: Adapter pattern for feature data access (frontend)
  Scope: frontend/src/app/core/adapters/, frontend/src/app/features/**
  Rule: New feature data-access layers should be implemented as an abstract adapter
  class (`Base<Feature>Adapter`) with an `Http<Feature>Adapter` and
  `Mock<Feature>Adapter` implementation, both `@Injectable({providedIn:'root'})`,
  wired via a provider in `app.config.ts`; components inject the abstract base class,
  never a concrete implementation.
  Why: Established for calendar (`BaseCalendarAdapter`/`MockCalendarAdapter`) and now
  replicated for user-admin; keeps components testable/swappable between mock and real
  backend without touching component code.
  How to apply: Mirror `frontend/src/app/core/adapters/calendar.adapter.ts` and
  `calendar-mock.ts` structure (no constructors, `inject()`, abstract class as DI
  token) for any new feature adapter.
  Evidence: WI-20260610-BACKEN-001,
  frontend/src/app/core/adapters/user-admin.adapter.ts,
  user-admin-http.adapter.ts, user-admin-mock.ts
