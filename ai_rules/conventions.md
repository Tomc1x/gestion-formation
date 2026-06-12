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

---

### CONV-004 — EntitySelectorComponent for entity multi-selection (search + pagination)

Scope: frontend/src/app/shared/components/entity-selector/*, any form/UI selecting from a potentially long list of entities (eleves, formateurs, prerequis, cours)
Origin: WI-20260611-FULLST-001, WI-20260611-FULLST-006
Added: 2026-06-11
Verified: 2026-06-11

**Rule / Decision / Pitfall:** Any new UI for selecting one or more entities from a list that can exceed ~15 items must use `EntitySelectorComponent` (text search, accent-insensitive, + pagination), never an exhaustive checkbox-list. The component's contract is `SelectableEntity = { id: number; label: string; sublabel?: string }`; mode `'add'` emits `add: output<number>()` per row action (immediate API call), mode `'multi-select'` emits `selectionChange: output<Set<number>>()` on toggle (deferred to parent form submit). It also supports a `disabledIds` input.

**Why:** Exhaustive checkbox-lists were identified as a recurring anti-pattern on promotions (FULLST-001) and the cours catalogue (FULLST-006) — unfilterable and illegible at scale. The component was implemented in FULLST-001 and reused/confirmed in FULLST-006.

**How to apply:** Map domain entities to `SelectableEntity[]` (business-domain filtering — cursus, disponibilite, role — happens in the calling component before passing `items`, the selector itself only does text search + pagination). Choose `mode: 'add'` vs `'multi-select'` based on whether the action triggers an immediate API call or is submitted with the parent form. Pass `disabledIds` to exclude already-selected/ineligible entities.

**Counter-indications:** Short, fixed lists (<~15 items) where an exhaustive checkbox-list remains readable do not need this component.

---

### CONV-005 — Badge-list truncation in admin tables via native `<details>/<summary>`

Scope: frontend/src/app/features/administration/**/*.html (table columns showing lists of related entities: formateurs, prerequis, eleves, etc.)
Origin: WI-20260611-FULLST-006
Added: 2026-06-11
Verified: 2026-06-11

**Rule / Decision / Pitfall:** Any table column displaying a list of related entities must cap visible badges at 3 + a "+X autres" badge that opens a native `<details><summary>+X autres</summary>...</details>` listing the rest, instead of rendering the full list in `.badge-list`.

**Why:** Unbounded badge lists overflow/stack and make the table unreadable as soon as an entity has more than a few relations (observed on the cours catalogue, FULLST-006). `<details>/<summary>` is accessible and requires no JS tooltip dependency.

**How to apply:** Extract a shared utility `visibleBadges(list, max=3)` / `hiddenCount(list, max=3)`; consider `frontend/src/app/shared/utils/badge-list.ts` if reused by a 3rd screen. Wrap the overflow in `<details><summary>+X autres</summary>...</details>`.

**Counter-indications:** None identified — applies to any admin table column with a potentially long related-entity list.

---

### CONV-006 — CoursPlanifie naming for the planning pivot entity (formerly PromotionCours)

Scope: backend/src/main/java/fr/eni/gestionformation/{entity,repository,dto,service,controller,exception} (course planning)
Origin: WI-20260611-FULLST-008
Added: 2026-06-11
Verified: 2026-06-11

**Rule / Decision / Pitfall:** The entity formerly named `PromotionCours` is now `CoursPlanifie` (status enum `CoursPlanifieStatut`, repository `CoursPlanifieRepository`, DTO `CoursPlanifieResponse`, exception `CoursPlanifieNotFoundException`). Any new code touching course planning must use this naming.

**Why:** Decouples "a planned course session" from "belonging to a promotion" to support cours-a-l'unite (see DEC-003).

**How to apply:** `grep -r "PromotionCours" backend/src` must return 0 results. New code referencing the planning pivot entity must use `CoursPlanifie*` names.

**Counter-indications:** None — superseded naming should not reappear in new code.

---

### CONV-007 — Manual cascade-delete pattern for required `@ManyToOne` FKs without JPA cascade

Scope: backend/src/main/java/fr/eni/gestionformation/service/*.java (any `deleteById` whose entity is the target of a non-cascading `optional=false`/`nullable=false` FK)
Origin: WI-20260611-FULLST-024, ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-024.md
Added: 2026-06-11
Verified: 2026-06-11

**Rule / Decision / Pitfall:** Before deleting an entity X that other entities Y reference via a required `@ManyToOne` (no `cascade`/`orphanRemoval`), find all Y rows referencing X via a `findByXId(...)` repository method and delete the deepest-dependent rows first (e.g. `InscriptionCours` before `CoursPlanifie` before `Cours`/`Promotion`), all within the same `@Transactional` service method, before the final `repository.delete(x)`.

**Why:** `InscriptionCours.coursPlanifie` and `CoursPlanifie.cours`/`.promotion` are all `optional=false`/`nullable=false` with no cascade, so naive deletion of `Cours`/`Promotion`/`CoursPlanifie` throws a raw `DataIntegrityViolationException` (FK violation), which this project's filter chain surfaces as an unhelpful empty-body 403 (see PIT-020).

**How to apply:** See `PromotionService.deleteInscriptionsForPlanning` and `CoursService.deleteById` (steps 2-3) for the reference implementation. Order matters: InscriptionCours -> CoursPlanifie -> parent (Cours/Promotion).

**Counter-indications:** Does not apply to cross-aggregate reference-nulling (e.g. `Cursus` deletion nulling `Promotion.cursus`) — that case is covered by DEC-002's sibling-service-call pattern, not by deep deletion chains.

---

### CONV-008 — Controller test pattern for `@WebMvcTest` with `SecurityConfig`

Scope: backend/src/test/java/fr/eni/gestionformation/controller/*ControllerTest.java
Origin: WI-20260611-FULLST-026, ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-026.md
Added: 2026-06-11
Verified: 2026-06-11

**Rule / Decision / Pitfall:** New controller tests must use `@WebMvcTest(XController.class)` + `@Import(SecurityConfig.class)`, mock the controller's service plus `JwtService` and `UserDetailsServiceImpl` with `@MockitoBean`, and authenticate requests via `SecurityMockMvcRequestPostProcessors.user(User.builder()...build())` rather than disabling security or using `@WithMockUser` (see PIT-017).

**Why:** Importing the real `SecurityConfig` makes controller tests also implicitly verify the role-based access rules declared there, instead of duplicating/drifting from them. Confirmed across 6 new controller test classes (UserAdminControllerTest, FiliereControllerTest, CursusControllerTest, InvitationControllerTest, InscriptionCoursControllerTest, PromotionControllerTest) added in WI-20260611-FULLST-026, consistent with the earlier PromotionControllerSecurityTest/AuthControllerTest.

**How to apply:** Copy the `@MockitoBean` list (the controller's service, `JwtService`, `UserDetailsServiceImpl`) and the `userWithRole(...)` helper from any of the controller tests listed above. Authenticate with `.with(user(userEntityWithRole))`, not `@WithMockUser`.

**Counter-indications:** None identified — applies to all new `@WebMvcTest` controller tests in this package.

---

### CONV-009 — Shared calculation logic in Angular goes to core/utils/*.util.ts as pure TS

Scope: frontend/src/app/core/utils/, composants administration
Origin: WI-20260611-FULLST-027
Added: 2026-06-11
Verified: 2026-06-11

**Rule / Decision / Pitfall:** When a calculation/transformation logic (alerts, data transformations) must be reused by multiple components, extract it into `core/utils/<domaine>.util.ts` as pure functions (no signals, no Angular injection), taking `core/models/*` types as input/output.

**Why:** Enables testing independent of the Angular lifecycle (e.g. a badge on the list page and an alerts block on the detail page compute the same thing) — implemented for `cursus-alerts.util.ts`.

**How to apply:** Create the file under `core/utils/`, export types + pure functions, call them from `computed()` in components.

**Counter-indications:** None identified — applies to any cross-component calculation logic.

---

### CONV-010 — Escape `$` as `$$` in `.env` values for docker-compose

Scope: .env, docker-compose.yml

Origin: WI-20260611-FULLST-030

Added: 2026-06-11

Verified: 2026-06-11

**Rule / Decision / Pitfall:** Any `.env` value containing a literal `$` (e.g. `JWT_SECRET`) must escape it as `$$`, otherwise docker-compose's variable interpolation (`${VAR}` syntax) partially consumes/corrupts the value before it reaches the container.

**Why:** WI-20260611-FULLST-030 found `JWT_SECRET` in `.env` contained `$` characters that docker-compose tried to interpolate as variable references, producing a wrong secret inside the container.

**How to apply:** When adding or editing `.env` values that contain `$`, write `$$` for each literal `$`. Verify the final resolved value with `docker compose config` before running `docker compose up`.

**Counter-indications:** Does not apply to values consumed directly by the app outside docker-compose (e.g. `application-local.properties`, `application.properties`) — only `.env` files read by docker-compose's interpolation engine are affected.
