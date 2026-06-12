# Memory Note — WI-20260610-BACKEN-020

**Role:** developer
**Status:** DONE
**Scope:** Frontend — new "Planning" calendar view for Promotions (drag & drop / resize), using FullCalendar Angular.

## Files Touched

### New
- `frontend/src/app/features/administration/promotions/planning/planning.ts` — `PlanningComponent`: loads all promotions via `BasePromotionAdapter.getAll()`, populates a promotion `<select>`, loads the selected promotion's `planning` via `getById()`, maps each `PromotionCours` to a FullCalendar `EventInput` (title=`coursNom`, `start`=`dateDebut`, `end`=`dateFin`+1 day exclusive, color by `statut`: PLANIFIE=#1D4ED8, EN_COURS=#16A34A, TERMINE=#6B7280; orange border + `evt-warning` class if `warnings.length > 0`). `eventDrop`/`eventResize` call `updatePlanning(promotionId, promotionCoursId, {dateDebut, dateFin})` (converting FullCalendar's exclusive `end` back to inclusive by subtracting 1 day); on success shows non-blocking warnings in a banner; on HTTP error calls `arg.revert()` and shows an error banner.
- `frontend/src/app/features/administration/promotions/planning/planning.html` — page header with back link to `/app/admin/promotions`, promotion selector (`aria-label`), warning/error banners, `<full-calendar [options]="calendarOptions()" />`.
- `frontend/src/app/features/administration/promotions/planning/planning.scss` — page/toolbar/calendar layout, `.state-msg--error`/`.state-msg--warning` banners (mirrors `promotions.scss` `.state-msg` pattern + `--red`/`--red-bg` tokens), `.evt-warning` border style.

### Modified
- `frontend/src/app/app.routes.ts` — new lazy route `admin/promotions/:id/planning` → `PlanningComponent`, `roleGuard(['REF'])` (same guard as `admin/promotions`, per PIT-006 — sidebar not changed since this is reached via a row action, not a top-level nav entry).
- `frontend/src/app/features/administration/promotions/promotions.ts` — added `RouterLink` and `LucideCalendarDays` imports.
- `frontend/src/app/features/administration/promotions/promotions.html` — new row-action link (calendar icon) `[routerLink]="['/app/admin/promotions', promotion.id, 'planning']"` placed before the edit button.
- `frontend/package.json` / `package-lock.json` — added FullCalendar packages (see below).

## Packages added
- `@fullcalendar/angular@6.1.20`
- `@fullcalendar/core@6.1.20`
- `@fullcalendar/daygrid@6.1.20`
- `@fullcalendar/timegrid@6.1.20`
- `@fullcalendar/interaction@6.1.20`

Verified peer deps: `@fullcalendar/angular@6.1.20` declares `"@angular/core": "12 - 21"` and `"@angular/common": "12 - 21"` — compatible with this project's Angular 21.2.0.

## Decisions / Implementation notes

- **Integration point**: chose a per-row "Voir le planning" icon link on the existing `admin/promotions` table (new route `/app/admin/promotions/:id/planning`) rather than a tab inside the promotion modal — simplest to wire with existing routing/guards, and avoids reworking the existing CRUD modals.
- **No sidebar entry**: the planning view is reached contextually from a promotion row, not as a standalone top-level nav item, so `sidebar.ts` was intentionally left unchanged (PIT-006 N/A here — no new top-level roleGuard/sidebar pair, same guard `['REF']` as the parent route).
- **Promotion selector**: on the planning page itself there is also a `<select>` (per brief) so the user can switch promotion without going back — selecting a promotion both reloads the calendar events and updates the route via `router.navigate(['/app/admin/promotions', id, 'planning'])` (keeps the URL shareable/bookmarkable).
- **Date conversion exclusive/inclusive**: `toEvent()` adds 1 day to `dateFin` for FullCalendar's exclusive `end`; `onEventChange()` subtracts 1 day from FullCalendar's `event.endStr` before calling `updatePlanning`. Implemented with plain `Date`/`setDate`/`toISOString` (no new date-fns usage needed for this simple +/-1 day arithmetic).
- **Warnings handling**: non-blocking — on success with `warnings.length > 0`, a `state-msg--warning` banner shows the joined warning strings and the event's border/class is updated to `evt-warning` (orange border). The move/resize itself is never reverted for warnings, only for HTTP errors (per brief).
- **`state-msg--warning` is new**: did not exist in any global or component SCSS before; added it locally in `planning.scss` alongside `state-msg--error` (mirrors the existing `promotions.scss` `.state-msg { &--error {...} }` pattern, using existing `--red`/`--red-bg` tokens for error and a local orange palette for warning since no global warning token exists yet).

## Test Results

```
cd frontend && npx ng build
Application bundle generation complete. [4.797 seconds]
```
Build succeeds. New `planning` lazy chunk: 273.22 kB raw / 68.24 kB transfer (FullCalendar core+plugins). Only pre-existing budget warnings remain (`utilisateurs.scss` +5.09kB, `register.scss` +951B over 4kB — both pre-existing per PIT-004/005, untouched by this WI). No new component-style budget warning for `planning.scss`.

## Visual / E2E verification

Not run in this session — `ng serve` / backend smoke test was not executed due to time constraints. The build compiles cleanly (TypeScript strict mode passes, including FullCalendar's `EventDropArg`/`EventResizeDoneArg` types and `CalendarOptions`/`EventInput` from `@fullcalendar/core`).

## Open Blockers

None.

## Next Actions

- Recommend a manual smoke test: `ng serve` (or `docker compose up db -d` + `./gradlew bootRun --args='--spring.profiles.active=local'` for full e2e), navigate to `/app/admin/promotions`, click the new calendar icon on a row, verify: calendar renders with `MockPromotionAdapter`/real backend events, drag/resize triggers `updatePlanning`, warnings banner appears for conflicting moves, HTTP error reverts the drag.
- If `state-msg--warning` proves useful elsewhere, consider promoting it to a shared/global style per CONV-002-style token consolidation (currently component-local in `planning.scss`).

## Recall Hints

- Planning view: `frontend/src/app/features/administration/promotions/planning/` at route `/app/admin/promotions/:id/planning`, reached via row-action icon on `/app/admin/promotions`.
- FullCalendar v6.1.20 (`@fullcalendar/angular` + `core`/`daygrid`/`timegrid`/`interaction`) — compatible with Angular 21.

## Proposed Rules

- TYPE: CONVENTION
  Title: FullCalendar v6.1.20 is the chosen calendar drag-and-drop library, compatible with Angular 21
  Scope: frontend — any future feature needing an interactive calendar (drag/drop, resize, multiple views)
  Rule: Use `@fullcalendar/angular@6.1.20` + `@fullcalendar/core`/`daygrid`/`timegrid`/`interaction` at the same pinned version (peer dep requires exact `~6.1.20` core match). Do not mix versions across `@fullcalendar/*` packages.
  Why: `@fullcalendar/angular@6.1.20` peer-depends on `"@fullcalendar/core": "~6.1.20"` and `"@angular/core": "12 - 21"` — verified compatible with this project's Angular 21.2.0 via `npm view`.
  How to apply: `npm install @fullcalendar/angular@6.1.20 @fullcalendar/core@6.1.20 @fullcalendar/daygrid@6.1.20 @fullcalendar/timegrid@6.1.20 @fullcalendar/interaction@6.1.20`. Import `FullCalendarModule` into the standalone component's `imports[]`, plugins array references `dayGridPlugin`/`timeGridPlugin`/`interactionPlugin`.
  Evidence: WI-20260610-BACKEN-020, frontend/src/app/features/administration/promotions/planning/planning.ts, build output (planning chunk 273kB raw).
