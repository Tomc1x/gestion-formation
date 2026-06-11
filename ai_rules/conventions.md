# Conventions

Durable conventions for this codebase.

---

### CONV-001 — Adapter pattern for feature data access (frontend)

Scope: frontend/src/app/core/adapters/, frontend/src/app/features/**
Origin: WI-20260610-BACKEN-001
Added: 2026-06-10
Verified: 2026-06-10

**Rule / Decision / Pitfall:** New feature data-access layers must be implemented as an abstract adapter class (`Base<Feature>Adapter`) plus a concrete real-backend implementation (`Http<Feature>Adapter` or `<Feature>ApiAdapter`) and a `Mock<Feature>Adapter`, all `@Injectable({providedIn:'root'})`, wired via a provider in `app.config.ts`; components must inject only the abstract base class, never a concrete implementation.

**Why:** Established for calendar (`BaseCalendarAdapter` / `CalendarApiAdapter` / `MockCalendarAdapter`) and now replicated identically for user-admin (`UserAdminAdapter` / `UserAdminHttpAdapter` / `UserAdminMockAdapter`). Keeps components testable/swappable between mock and real backend without touching component code.

**How to apply:** Mirror `frontend/src/app/core/adapters/calendar.adapter.ts` and `calendar-mock.ts` structure: abstract class as DI token, no constructors, use `inject()`, provide the chosen implementation in `app.config.ts`. The naming suffix for the real implementation may be `Http<Feature>Adapter` or `<Feature>ApiAdapter` (both seen in the codebase) — pick one consistent with the nearest sibling feature.

**Counter-indications:** Does not apply to one-off utility services with no mock/real backend distinction (e.g. pure UI state services).

---

### CONV-003 — HTTP mapping of business exceptions in GlobalExceptionHandler

Scope: backend/src/main/java/fr/eni/gestionformation/exception/
Origin: WI-20260610-BACKEN-004
Added: 2026-06-10
Verified: 2026-06-10

**Rule / Decision / Pitfall:** Every new business exception must get a dedicated `@ExceptionHandler` method in `GlobalExceptionHandler`, returning `ResponseEntity<String>` with the semantically correct HTTP status (404 Not Found for missing resources, 409 Conflict for duplicate/already-exists, 422 Unprocessable Entity for business-rule/graph violations such as cycles, 400 Bad Request for invalid input like an invitation token).

**Why:** Already followed consistently for Filiere/Cursus/Cours/User/CycleDetected exceptions; consistency lets the frontend rely on status codes to branch error handling.

**How to apply:** Add `@ExceptionHandler(XxxException.class)` returning `ResponseEntity.status(HttpStatus.XXX).body(ex.getMessage())`, mirroring the existing handlers in `GlobalExceptionHandler.java`. Pick the status by the nature of the violation: 404 = entity not found, 409 = conflict/duplicate, 422 = business-rule/invariant violation, 400 = malformed/invalid input.

**Counter-indications:** None identified — applies to all new business exceptions in this package.

**Note:** Before picking an `HttpStatus` constant, check for Spring Framework 7 renames — see PIT-007 (e.g. `UNPROCESSABLE_ENTITY` → `UNPROCESSABLE_CONTENT`).

---

### CONV-002 — Single source of truth for design tokens (SCSS var + CSS custom property)

Scope: frontend/src/styles/_variables.scss, frontend/src/styles.scss (`:root` block)

Origin: WI-20260610-BACKEN-002

Added: 2026-06-10
Verified: 2026-06-10

**Rule / Decision / Pitfall:** Every design token (color, shadow, radius, spacing, font, etc.) must be defined first as a SCSS variable in `_variables.scss`, then exposed as a CSS custom property in the `:root` block of `styles.scss` via `--name: #{$name}`. Never define a raw value directly in `:root` or inside a component stylesheet.

**Why:** `$e1/$e2/$e3` (shadow tokens) existed in `_variables.scss` but were not exposed as `--e1/--e2/--e3` custom properties, making them unusable via `var(--e1)` in global SCSS or components. This caused desynchronization between SCSS-only and CSS-custom-property-based usages.

**How to apply:** Before adding a new design variable, check whether its SCSS equivalent already exists in `_variables.scss`. If not, create both the SCSS variable and the `:root` custom property mapping in the same change. Components should consume tokens via `var(--token-name)`.

**Counter-indications:** None — applies to all new global design tokens. Does not apply to component-local, non-reusable values.
