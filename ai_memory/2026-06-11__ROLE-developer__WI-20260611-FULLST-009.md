# WI-20260611-FULLST-009 — Frontend: Calendrier eleve agrege + vue formateur inscrits

## Work Item
WI-20260611-FULLST-009

## Role
developer

## Status
READY_FOR_REVIEW

## Scope
1. Adapt the shared calendar component (`mon-calendrier`) so a connected ELEVE
   sees their aggregated planning (promotion sessions + individual
   inscriptions) via `GET /api/eleves/{id}/planning`, with the existing
   blue/green color coding (PROMOTION/INDIVIDUEL).
2. Create a FORMATEUR page listing students enrolled in a given
   `CoursPlanifie` via `GET /api/cours-planifies/{id}/inscrits`, with a
   PROMOTION/INDIVIDUEL badge per student.
3. Create the necessary HTTP adapters following existing conventions.
4. Add routes with `roleGuard` (ETUDIANT for the calendar — already
   unrestricted/all-roles, FORMATEUR for the inscrits page).

## Files Touched
- `frontend/src/app/core/models/inscription.model.ts` (new) — `PlanningEleve`,
  `InscritCours`, `OrigineInscription`.
- `frontend/src/app/core/models/calendar-event.model.ts` — added optional
  `origine?: 'PROMOTION' | 'INDIVIDUEL'`.
- `frontend/src/app/core/adapters/eleve-planning-http.adapter.ts` (new) —
  `HttpElevePlanningAdapter extends BaseCalendarAdapter`, calls
  `GET /api/eleves/{currentUserId}/planning`, maps `PlanningEleve` ->
  `CalendarEvent` (sets `promotion = coursNom` when `origine === 'PROMOTION'`
  to reuse existing color-coding logic in the template/SCSS). Returns `[]`
  when `currentUserId()` is `null`.
- `frontend/src/app/core/adapters/inscription.adapter.ts` (new) —
  `BaseInscriptionAdapter` abstract class.
- `frontend/src/app/core/adapters/inscription-http.adapter.ts` (new) —
  `HttpInscriptionAdapter`, calls `GET /api/cours-planifies/{id}/inscrits`.
- `frontend/src/app/features/formateur/inscrits/{inscrits.ts,html,scss}` (new)
  — read-only page, reads `:id` route param (coursPlanifieId), displays a
  table (Nom/Prenom/Origine) with PROMOTION (blue) / INDIVIDUEL (green) badge.
- `frontend/src/app/app.routes.ts` — added route
  `cours-planifies/:id/inscrits`, `roleGuard(['FORMATEUR'])`, lazy-loaded
  `InscritsComponent`.
- `frontend/src/app/app.config.ts` — `BaseCalendarAdapter` now provides
  `HttpElevePlanningAdapter` (was `MockCalendarAdapter`); registered
  `BaseInscriptionAdapter` -> `HttpInscriptionAdapter`.
- `frontend/src/app/core/services/auth.service.ts` — **out-of-scope but
  necessary fix** (see Decisions below): `_currentUser`/`_currentRole` are now
  rehydrated from `localStorage['user']` on service construction (if a token
  exists), and `uid` is now persisted to `localStorage['user']` at login.
  `logout()` also clears `localStorage['user']`.

## Evidence
- `cd frontend && npx ng build` -> BUILD SUCCESSFUL. Pre-existing SCSS budget
  warnings on `register`, `utilisateurs`, `planning` (unrelated to this WI,
  unchanged).
- chrome-devtools on `http://localhost:4200`:
  - `/app/calendrier` renders correctly (legend "Cours de promotion" / "Cours
    à l'unité", month grid, no console errors).
  - Logged in as `admin@admin.com` / `Admin123` (only seeded account, no demo
    accounts per project policy). After reload, sidebar correctly shows
    "Utilisateurs" (ADMIN-only route) — confirms the AuthService rehydration
    fix works for role.
  - `localStorage['user']` after fresh login: `{"uid":null,"role":"ADMINISTRATEUR",...}`
    — backend login response returns `uid: null` for this admin account, so
    `currentUserId()` stays `null` even after the rehydration fix. The
    `/api/eleves/{id}/planning` call could therefore not be observed in the
    network tab in this session (adapter degrades to `[]` gracefully, by
    design).
  - `/app/cours-planifies/:id/inscrits` not visually verified (no FORMATEUR
    test account available); route registration and build are correct.

## Decisions
- Reused the existing `event.promotion` field / CSS classes
  (`event-chip--promo`/`event-chip--unit`, `event-card--promo`/`--unit`) for
  color coding by setting `promotion = coursNom` when `origine === 'PROMOTION'`,
  rather than introducing new template branches — minimal diff, consistent
  with WI-20260608-FRONTE-002 color convention (#1D4ED8 / #16A34A).
- `HttpElevePlanningAdapter.getEvents()` filters sessions client-side by the
  `getDateRange()` window (from `BaseCalendarAdapter`) since the backend
  endpoint returns the full planning, not paginated by view/date.
- Created a minimal `BaseInscriptionAdapter` with a single `getInscrits()`
  method (no mock adapter created — no other module currently provides a mock
  alongside HTTP unless actively used; HTTP-only matches `promotion-http.adapter`
  pattern which also has no mock).
- **AuthService fix** (auth.service.ts): pre-existing bug — only
  `_isAuthenticated` was restored from `localStorage` on service init;
  `_currentUser`/`_currentRole` always reset to defaults (`null`/`'REF'`) on
  page reload, regardless of the actual logged-in user. This silently broke
  any feature depending on `currentUserId()` or `currentRole()` after reload
  (header already displayed wrong role/name post-reload). Since
  `HttpElevePlanningAdapter` (this WI's core deliverable) hard-depends on
  `currentUserId()`, fixed by: (1) persisting `uid` in `localStorage['user']`
  at login, (2) reading `localStorage['user']` in field initializers to seed
  `_currentUser`/`_currentRole` if a token exists, (3) clearing
  `localStorage['user']` on logout. Scoped to `auth.service.ts` only, no
  behavior change for other consumers beyond making them now correctly survive
  reload.

## Open Blockers
- No ETUDIANT or FORMATEUR test account available in the local DB
  (`DataInitializer` only seeds `admin@admin.com`, and project policy
  forbids adding demo accounts). Full visual/functional verification of both
  new screens (eleve calendar with real promo+individual events, formateur
  inscrits list) requires either:
  1. A test ETUDIANT/FORMATEUR account provisioned by another channel
     (manager/engineer decision — not created by developer per policy), or
  2. Backend investigation into why `admin@admin.com`'s login response has
     `uid: null` (separate, possibly pre-existing backend issue, out of scope
     for this frontend WI).

## Next Actions
- Manager/engineer to provide or point to ETUDIANT and FORMATEUR credentials
  (or fix the `uid: null` issue for an existing account) so a
  frontend-reviewer-analyst (or developer) can complete end-to-end visual
  verification of both screens with real backend data (CoursPlanifie +
  InscriptionCours fixtures).
- Once verified, flip WI-20260611-FULLST-009 status to DONE.

## Recall Hints
- Endpoints: `GET /api/eleves/{id}/planning`, `GET /api/cours-planifies/{id}/inscrits`
  (see WI-20260611-FULLST-008 in WORK_ITEMS.md for exact response shapes).
- Color convention PROMOTION=#1D4ED8 / INDIVIDUEL=#16A34A defined in
  `mon-calendrier.scss` (from WI-20260608-FRONTE-002).
- `AuthService.currentUserId()` now correctly rehydrates after reload — any
  future WI relying on it should work, but only for accounts whose login
  response includes a non-null `uid`.

## Proposed Rules
- TYPE: PITFALL
  Title: AuthService currentUser/currentUserId not rehydrated pre-fix could recur if reverted
  Scope: frontend/src/app/core/services/auth.service.ts and any adapter using
    `authService.currentUserId()` or `authService.currentRole()`
  Rule: `_currentUser` and `_currentRole` must be initialized from
    `localStorage['user']` (not just `_isAuthenticated` from the token), and
    `localStorage['user']` must include `uid`, otherwise any feature that
    depends on the current user's id/role silently breaks after a page reload.
  Why: Found while implementing WI-009 — HttpElevePlanningAdapter returned []
    after reload because currentUserId() was always null post-reload before
    this fix.
  How to apply: When adding new AuthService fields persisted across reload,
    always update both the login `tap()` (write to localStorage) and the
    constructor/field-initializer rehydration logic together.
  Evidence: frontend/src/app/core/services/auth.service.ts (this WI's diff)

- TYPE: PITFALL
  Title: admin@admin.com login response has uid: null
  Scope: backend login endpoint / DataInitializer seeded admin account
  Rule: The only seeded test account (admin@admin.com) returns `uid: null`
    in the `/api/auth/login` response, breaking any frontend feature keyed on
    the current user's id when tested with this account.
  Why: Blocked full verification of WI-009 (eleve planning adapter needs a
    real eleveId).
  How to apply: Backend team to investigate why AuthResponse.uid is null for
    this user (likely missing id mapping in the auth controller/service for
    the seeded admin), or provide a non-admin test account with a populated
    uid.
  Evidence: localStorage['user'] after fresh admin login = {"uid":null,...}
    (captured via chrome-devtools evaluate_script during WI-009 verification).
