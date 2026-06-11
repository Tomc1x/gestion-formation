# Pitfalls

Known pitfalls, anti-patterns, and traps encountered in this codebase.

---

### PIT-001 — Lucide Angular: imports[] vs data usage

Scope: Angular components using @lucide/angular icon components
Origin: WI-20260605-WRAPPE-001, ai_memory/2026-06-05__ROLE-developer__WI-20260605-WRAPPE-001.md
Added: 2026-06-05
Verified: 2026-06-05

**Pitfall:** Including a Lucide icon in the component `imports[]` array when it is only used as a data value (TypeScript array entry) and never appears as an attribute directive in the HTML template causes Angular compiler error NG8113 (unused import treated as build error in strict mode).

**Why:** Angular's compiler validates that every entry in `imports[]` is actually referenced as a directive or pipe in the template. Icons passed as data (e.g., `{ icon: LucideCalendar }`) satisfy TypeScript typing but produce no template reference, so the compiler flags them as unused.

**How to apply:** Before adding a Lucide icon to `imports[]`, verify it appears as an attribute directive in the HTML template (e.g., `<lucide-icon lucideCalendar ...>`). If the icon only appears in a `.ts` data array and never as a `lucide*` attribute in the template, do NOT add it to `imports[]` — the `LucideIconInput` type alone is sufficient for the data usage.

**Counter-indications:** Icons used both as data values AND as template directives must remain in `imports[]`.

---

### PIT-002 — Angular SCSS: root-level selectors inside @media blocks

Scope: Angular component SCSS files using BEM or nested selectors
Origin: WI-20260605-WRAPPE-001, ai_memory/2026-06-05__ROLE-developer__WI-20260605-WRAPPE-001.md
Added: 2026-06-05
Verified: 2026-06-05

**Pitfall:** Redeclaring a top-level component selector (e.g., `.sidebar { }`) at root level inside a `@media` block in an Angular component SCSS file causes the mobile overrides to be silently ignored, because Angular view encapsulation appends an attribute selector (e.g., `[_ngcontent-xxx]`) that the root-level media-query selector does not carry.

**Why:** Angular's `ViewEncapsulation.Emulated` rewrites all selectors in the component stylesheet to include a unique attribute. A selector written at root level inside `@media` bypasses the nesting context where that attribute was established, producing a selector that never matches any element in the rendered DOM.

**How to apply:** Always write responsive overrides nested inside the original SCSS structure: `@media (...) { .parent { .child { ... } } }`. Alternatively, place the `@media` rule inside the existing nested block so the encapsulation attribute is inherited. Never hoist a component selector to root level within a `@media` block.

**Counter-indications:** Global stylesheets (`styles.scss`, not component-scoped files) do not use view encapsulation and are unaffected by this rule.

---

### PIT-003 — Filière API endpoint is singular `/api/filiere`

Scope: backend (FiliereController) and any frontend code/adapters calling the Filière API
Origin: WI-20260610-BACKEN-007
Added: 2026-06-10
Verified: 2026-06-10

**Rule / Decision / Pitfall:** The real REST endpoint for Filière resources is `/api/filiere` (singular), as declared by `@RequestMapping("/api/filiere")` in `FiliereController.java:14` — not `/api/filieres`.

**Why:** Inconsistent pluralization between backend route naming and typical REST conventions causes frontend calls to silently 404 if `/api/filieres` is assumed.

**How to apply:** When writing or reviewing frontend services/adapters that call the Filière API, always use `/api/filiere` (singular). Verify against `FiliereController.java` if unsure, since this does not follow the usual plural-resource convention.

**Counter-indications:** Other controllers in this codebase may use plural routes; this singular form is specific to Filière.

### PIT-004 — Angular CSS budgets are measured on compiled CSS, not SCSS source

Scope: frontend (all components with `anyComponentStyle` budgets in angular.json)
Origin: WI-20260610-BACKEN-008
Added: 2026-06-10
Verified: 2026-06-10

**Rule / Decision / Pitfall:** Angular's `anyComponentStyle` budget is enforced against the compiled CSS output, not the `.scss` source size — comments, indentation, and SCSS variable names have zero impact.

**Why:** A first reduction attempt (shortening Unicode comment banners) produced no measurable effect because these are stripped at compile time.

**How to apply:** To measure real size, compile with `npx sass <file>.scss out.css --style=compressed` and check the output file size. To actually reduce size, factor duplicated declarations into SCSS `%placeholder` + `@extend`, which merges selectors (`.a, .b, .c { ... }`) instead of duplicating declaration blocks.

**Counter-indications:** None — applies to any SCSS file subject to an Angular component style budget.

### PIT-005 — Modal/form SCSS pattern duplicated across admin components

Scope: frontend/src/app/features/administration/{utilisateurs,cours,cursus}
Origin: WI-20260610-BACKEN-008
Added: 2026-06-10
Verified: 2026-06-10

**Rule / Decision / Pitfall:** `.modal*`, `.form-field`, `.form-row`, `.btn-secondary`, `.btn-danger`, focus-rings, and form inputs are duplicated near-identically across `utilisateurs.scss`, `cours.scss`, and `cursus.scss`.

**Why:** This duplication is the direct cause of `utilisateurs.scss` exceeding its CSS budget even after internal `%placeholder`/`@extend` factoring (residual ~1 kB over after a temporary budget bump from 8kB to 12kB in angular.json on 2026-06-10).

**How to apply:** When touching any of these 3 admin components, consider extracting a shared partial SCSS (e.g. `_admin-modal.scss`) for the common modal/form/button/focus-ring rules, imported by all three. Worth doing as a dedicated tier simple/medium WI rather than ad-hoc — verify with `grep -rl "modal-overlay\|form-field\|btn-secondary" frontend/src/app --include=*.scss` before starting.

**Counter-indications:** Do not migrate these components' color/spacing values to the global design system tokens (`--ink`, `--blue-800`, `--line`, etc.) as part of this — `utilisateurs.scss` intentionally uses a different local palette (see CONV note on visual-parity constraint, WI-20260610-BACKEN-008).

---

### PIT-006 — Manual sync required between app.routes.ts roleGuard and sidebar.ts roles

Scope: frontend/src/app/app.routes.ts and frontend/src/app/.../sidebar.ts (or equivalent navigation config)
Origin: WI-20260610-BACKEN-022, ai_memory/2026-06-10__ROLE-developer__WI-20260610-BACKEN-022.md
Added: 2026-06-10
Verified: 2026-06-10

**Pitfall:** A route's `roleGuard([...])` in `app.routes.ts` and the corresponding sidebar entry's `roles` field in `sidebar.ts` are two independent, manually-maintained lists. Changing one without the other causes silent desynchronization (a sidebar link shown to a role that cannot access the route, or hidden from a role that can) — there is no compile-time error.

**Why:** Both lists encode the same authorization intent but are not derived from a single source, so the TypeScript compiler cannot detect drift between them.

**How to apply:** Whenever you add, remove, or change a `roleGuard([...])` array on a route in `app.routes.ts`, immediately update the matching entry's `roles` field in `sidebar.ts` (and vice versa). When reviewing such a change, grep both files for the route path to confirm the role lists match.

**Counter-indications:** None — applies to any route protected by `roleGuard` that also has a sidebar navigation entry.

---

### PIT-007 — HttpStatus constants renamed in Spring Framework 7

Scope: backend/src/main/java/fr/eni/gestionformation/exception/GlobalExceptionHandler.java and any new `@ExceptionHandler`
Origin: WI-20260610-BACKEN-023, ai_memory/2026-06-10__ROLE-developer__WI-20260610-BACKEN-023.md
Added: 2026-06-10
Verified: 2026-06-10

**Pitfall:** `HttpStatus.UNPROCESSABLE_ENTITY` is deprecated since Spring Framework 7.0.7 (Spring Boot 4.0.6) in favor of `HttpStatus.UNPROCESSABLE_CONTENT` (same 422 code). Other constants were also renamed (e.g. `PAYLOAD_TOO_LARGE` → `CONTENT_TOO_LARGE`).

**Why:** Using deprecated constants triggers compiler warnings now and may be removed in a future Spring major version, causing a build break.

**How to apply:** Before using an `HttpStatus` constant in a new `@ExceptionHandler` (see CONV-003), check whether it has a renamed replacement in the current Spring version (e.g. `UNPROCESSABLE_CONTENT` instead of `UNPROCESSABLE_ENTITY`). Prefer the non-deprecated constant even if both resolve to the same numeric code.

**Counter-indications:** None — applies to any new or modified HTTP status mapping in this codebase.

---

### PIT-008 — PromotionController.toResponse always returns warnings=[] on GET

Scope: backend/src/main/java/fr/eni/gestionformation/controller/PromotionController.java, frontend planning/calendrier UI
Origin: WI-20260611-FULLST-005, WI-20260611-FULLST-010
Added: 2026-06-11
Verified: 2026-06-11

**Pitfall:** `GET /api/promotions/{id}` always returns `warnings: []`. Conflict warnings are only computed by `PUT .../planning/{id}` (`updatePlanning`). A developer implementing a read-only planning/calendar view may look for warnings at load time and find none.

**Why:** Source of confusion identified during the read-only calendar implementation (FULLST-005); confirmed unchanged in FULLST-010.

**How to apply:** Any UI displaying conflict indicators must accept that warnings are absent on initial load (display a help text instead, e.g. "conflicts are detected on save"), and only show warnings after a successful `updatePlanning` call with `warnings.length > 0`. If a read-only "check conflicts" view is needed in the future, request a dedicated read-only conflict-check endpoint.

**Counter-indications:** None — applies to all current consumers of `GET /api/promotions/{id}`.

---

### PIT-009 — InscriptionCours must have a DB-level unique constraint on (user_id, cours_planifie_id)

Scope: backend/src/main/java/fr/eni/gestionformation/entity/InscriptionCours.java
Origin: WI-20260611-FULLST-007, ai_doc/ANALYSIS__WI-20260611-FULLST-007__cours-planifie-inscription.md
Added: 2026-06-11
Verified: 2026-06-11

**Pitfall:** `InscriptionCours` must declare `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "cours_planifie_id"}))` at creation time, not rely on an application-level check alone.

**Why:** Without a DB-level constraint, duplicate enrollments (or redundant promo + individual enrollment for the same session) can occur, skewing headcount aggregations (see DEC-003).

**How to apply:** When creating or reviewing `InscriptionCours`, verify the unique constraint is present on the entity annotation. Application-level duplicate checks are a UX nicety, not a substitute.

**Counter-indications:** None.

---

### PIT-010 — ddl-auto=update + entity rename leaves an orphan table

Scope: backend (JPA/Hibernate, local profile, ddl-auto=update)
Origin: WI-20260611-FULLST-008
Added: 2026-06-11
Verified: 2026-06-11

**Pitfall:** Renaming a JPA entity/table (e.g. `PromotionCours` -> `CoursPlanifie`, see CONV-006) under `ddl-auto=update` creates the new table but does not drop the old one — the old table (`promotion_cours`) remains orphaned in the local database.

**Why:** The project has no Flyway/Liquibase migration tooling, so Hibernate's schema update is additive-only.

**How to apply:** After any entity/table rename, document the manual `DROP TABLE <old_name>` to run in local dev in the role note. If renames become frequent, consider introducing Flyway.

**Counter-indications:** Does not apply if/when the project adopts a migration tool that handles renames explicitly.

---

### PIT-011 — Concurrent agents editing the same backend file without worktree isolation

Scope: backend/src/main/java/fr/eni/gestionformation/service/* and any shared service/controller touched by multiple in-flight WIs
Origin: WI-20260611-FULLST-001
Added: 2026-06-11
Verified: 2026-06-11

**Pitfall:** When two work items touch the same backend service/controller in the same working tree (no `isolation: worktree`), a developer agent may start from a stale read and produce edits that don't compile against another agent's concurrent rename/refactor.

**Why:** During FULLST-001, `PromotionService.java`/`PromotionController.java` referenced renamed types (`PromotionCours` -> `CoursPlanifie`, see CONV-006) introduced concurrently by FULLST-008 — caught only because `./gradlew test` recompiles the whole module.

**How to apply:** Before finalizing a change to a shared backend file, re-read the touched shared files and run a full build/compile check (not just a targeted test) to catch cross-agent type/name mismatches. For multi-agent parallel work on the same module, prefer `isolation: worktree` per agent, or have the manager serialize edits to shared service/controller files.

**Counter-indications:** None — applies whenever multiple WIs run in parallel without worktree isolation on overlapping backend files.

---

### PIT-012 — DevTools "[issue]" on a page can point to the shared layout, not the page under test

Scope: frontend/src/app/layouts/main-layout/**, any chrome-devtools "[issue]" audit on an admin page
Origin: WI-20260611-FULLST-002
Added: 2026-06-11
Verified: 2026-06-11

**Pitfall:** Before fixing a page component because of a DevTools-reported issue, check the issue's `data.violatingNodeAttribute`/`uid` — it may point to an element of the shared header/sidebar (e.g. the global search input) present on every page, not the page component itself.

**Why:** During FULLST-002, 2 issues reported for the Promotions modal actually originated from the global header search input (`frontend/src/app/layouts/main-layout/header/header.html`), not the modal.

**How to apply:** Use `list_console_messages` then `get_console_message` to read `data.violatingNodeAttribute`/`uid`, and `take_snapshot` to identify the real element before editing the page component.

**Counter-indications:** None — applies to any DevTools audit on a page that includes the shared main layout.

---

### PIT-013 — Calendar grid logic duplicated between mon-calendrier and planning

Scope: frontend/src/app/features/calendrier/mon-calendrier/*, frontend/src/app/features/administration/promotions/planning/*
Origin: WI-20260611-FULLST-005
Added: 2026-06-11
Verified: 2026-06-11

**Pitfall:** The calendar grid logic (`monthGrid`, `weekDays`, `eventsByDay`, `navTitle`, prev/next navigation, `weekDayLabels`, `isCurrentMonth`) is duplicated near-identically (modulo event type) between `mon-calendrier.ts` and `planning.ts`. Any bug fix or UX change to the grid (first day of week, "today" highlight, accessibility) must be applied to BOTH files or the two views will visually diverge.

**Why:** FULLST-005 chose duplicated portage over extracting a shared `shared/components/month-week-grid/` component, for lack of budget to refactor `mon-calendrier` (used by eleves/formateurs, out of scope) without regression risk.

**How to apply:** When a future WI touches either calendar, evaluate extracting `shared/components/month-week-grid/` (design documented in `ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-005.md`, section "Composant partage month-week-grid") if divergence becomes costly. At minimum, grep both files before modifying grid logic.

**Counter-indications:** None until the shared component is extracted.

---

### PIT-014 — ng serve (esbuild) fails project-wide on any TS error, even in unrelated lazy routes

Scope: frontend (Angular 21 / esbuild dev-server)
Origin: WI-20260611-FULLST-005
Added: 2026-06-11
Verified: 2026-06-11

**Pitfall:** Even with lazy loading, `ng serve` (Angular 21, esbuild) fails entirely ("Application bundle generation failed") if ANY file in the project has a TS error, even in a lazy route never visited. Visual verification of an isolated feature is impossible while any other part of the project fails to compile.

**Why:** During FULLST-005, `planning.ts`/`.html` compiled cleanly, but `ng serve` refused to start due to preexisting out-of-scope TS errors in `promotions.ts`/`promotion-detail.ts` (FULLST-001/002 WIP), blocking visual verification of `/app/admin/promotions/:id/planning`.

**How to apply:** Before a chrome-devtools visual verification of a feature, run `ng build` (or `ng serve`) globally and confirm there are NO errors anywhere in the project — if there are, report the blocker to the manager rather than debugging the feature itself.

**Counter-indications:** None.

---

### PIT-015 — AuthService must rehydrate currentUser/currentUserId (incl. uid) from localStorage on reload

Scope: frontend/src/app/core/services/auth.service.ts and any adapter using `authService.currentUserId()` or `authService.currentRole()`
Origin: WI-20260611-FULLST-009
Added: 2026-06-11
Verified: 2026-06-11

**Pitfall:** `_currentUser` and `_currentRole` must be initialized from `localStorage['user']` (not just `_isAuthenticated` from the token), and `localStorage['user']` must include `uid`. Otherwise any feature depending on the current user's id/role silently breaks after a page reload (`currentUserId()` returns `null`).

**Why:** Found during FULLST-009 — `HttpElevePlanningAdapter` returned `[]` after reload because `currentUserId()` was always `null` post-reload before this fix.

**How to apply:** When adding new AuthService fields persisted across reload, always update both the login `tap()` (write to localStorage) and the constructor/field-initializer rehydration logic together.

**Counter-indications:** None.

---

### PIT-016 — Frontend models lack `Filiere.couleur` / `Promotion.statut` / `Promotion.dateFin` assumed by design references

Scope: frontend/src/app/core/models/cursus.model.ts, frontend/src/app/core/models/promotion.model.ts
Origin: WI-20260611-FULLST-018, ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-018.md
Added: 2026-06-11
Verified: 2026-06-11

**Pitfall:** UI design references for Filière/Promotion screens assume `filiere.couleur`, `promotion.statut`, and `promotion.dateFin` exist on the frontend models, but these fields do not exist in `cursus.model.ts` / `promotion.model.ts`.

**Why:** Implementing these as new persisted backend fields would be a much larger, uncoordinated cross-module change than the design intent required.

**How to apply:** Before implementing a UI design referencing these fields, grep `cursus.model.ts`/`promotion.model.ts` to confirm. If missing: derive `couleur` client-side via the `FILIERE_COLORS` deterministic-palette pattern (frontend-only), and derive `statut`/`dateFin` client-side from `dateDebut`/`planning[].dateFin`. Document the gap in the work item; only raise a new backend WI if a persisted field is genuinely required.

**Counter-indications:** If a future requirement needs these values server-side (e.g. for filtering/sorting in API queries), a backend WI to add real persisted fields may be justified — don't force client-side derivation in that case.

---

### PIT-017 — `@WithMockUser` unreliable with `@WebMvcTest` + `@Import(SecurityConfig.class)` on this project

Scope: backend/src/test/java/fr/eni/gestionformation/controller/*SecurityTest.java, any `@WebMvcTest` importing `SecurityConfig`
Origin: WI-20260611-FULLST-019, ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-019.md
Added: 2026-06-11
Verified: 2026-06-11

**Pitfall:** In controller security slice tests (`@WebMvcTest(...)` with `@Import(SecurityConfig.class)`), `@WithMockUser` produces a 403 on every request regardless of role — even `GET` endpoints mapped with `.authenticated()` only.

**Why:** This project's `SecurityConfig` declares a custom `DaoAuthenticationProvider` + `UserDetailsServiceImpl` (Spring Boot 4.0.6 / Spring Security 7.0.5). The `SecurityContext` populated by `@WithMockUser` is not consumed correctly in this configuration, so authorization always falls through to denied.

**How to apply:** Use `SecurityMockMvcRequestPostProcessors.user(userDetails)` instead, where `userDetails` is a real `UserDetails`-implementing entity (e.g. the project's `User` entity) carrying the role to test: `mockMvc.perform(post(...).with(user(userEntityWithRole)))`. Verified working for both authorized (200) and forbidden (403) cases — see `PromotionControllerSecurityTest`.

**Counter-indications:** None known — applies to any `@WebMvcTest` importing this project's `SecurityConfig`.

---

### PIT-018 — Verify the running backend process is up to date before diagnosing a SecurityConfig bug

Scope: backend (local dev, manually-started `gradlew bootRun`/`bootJar` process, e.g. on :8080)
Origin: WI-20260611-FULLST-019, ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-019.md
Added: 2026-06-11
Verified: 2026-06-11

**Pitfall:** A reproduced 403 / unexpected security behavior may be caused by a stale running backend process executing an older build, not by the current `SecurityConfig` source code.

**Why:** During FULLST-019, `SecurityConfig.java` already contained the correct `/api/promotions/**` rules (uncommitted changes from a prior session), but the backend process on :8080 was still running a jar built before that change, so it fell back to `anyRequest().authenticated()` and returned 403. This same pattern was previously observed during BACKEN-024.

**How to apply:** Before modifying security/config code in response to a reported 403 or auth anomaly, run `git status` + `git diff` on `SecurityConfig.java` (and related security files) to check for uncommitted changes not yet reflected in the running process. If the diff is non-empty and relevant, rebuild (`./gradlew bootJar`) and restart the process before concluding there is a code bug.

**Counter-indications:** Does not apply to CI/deployed environments where the running artifact is guaranteed to match the checked-out commit.

---

### PIT-019 — No centralized helper to get the current authenticated user (IDOR checks rely on manual cast)

Scope: backend/src/main/java/fr/eni/gestionformation/controller/**
Origin: WI-20260611-FULLST-023, ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-023.md
Added: 2026-06-11
Verified: 2026-06-11

**Pitfall:** There is no `CurrentUserService` or `@AuthenticationPrincipal`-based helper in this codebase. To fix an IDOR (e.g. `GET /api/eleves/{id}/planning` letting any authenticated user read another eleve's planning), the only available pattern is to inject `Authentication authentication` into the controller method and cast `authentication.getPrincipal()` to `fr.eni.gestionformation.entity.User` (which implements `UserDetails` via `UserDetailsServiceImpl.loadUserByUsername` and exposes `getUid()`).

**Why:** `UserDetailsServiceImpl` returns the `User` entity directly as the principal, so this cast is safe and is now the only documented way to compare the requester's identity against a path-variable resource id. Without this rule, future endpoints needing the same ownership check risk inventing inconsistent or incorrect patterns.

**How to apply:** Add `Authentication authentication` as a controller method parameter, then:
```java
if (!(authentication.getPrincipal() instanceof User currentUser) || !currentUser.getUid().equals(id)) {
    throw new AccessDeniedException("...");
}
```
No dedicated handler is needed in `GlobalExceptionHandler` — Spring Security's `ExceptionTranslationFilter` translates `AccessDeniedException` to HTTP 403 by default.

**Counter-indications:** If a `CurrentUserService` or `@AuthenticationPrincipal CurrentUser` abstraction is introduced later, this entry should be deprecated in favor of that convention.

---

### PIT-020 — Empty-body 403 on a write endpoint may be an unhandled DataIntegrityViolationException, not an auth bug

Scope: backend/src/main/java/fr/eni/gestionformation/service/PromotionService.java (deleteById), DB table `promotion_cours` (see PIT-010)
Origin: WI-20260611-FULLST-024, ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-024.md
Added: 2026-06-11
Verified: 2026-06-11

**Pitfall:** A `DELETE /api/promotions/{id}` (or similar) request can return an empty-body 403 with standard Spring Security headers even though the real cause is an uncaught `DataIntegrityViolationException` (FK violation), not an authorization failure — this project's filter chain surfaces unhandled exceptions from this code path as 403 instead of 500.

**Why:** WI-20260611-FULLST-022 logged this as an unidentified "Bug additionnel" (403 on promotion delete) and left it unresolved. WI-20260611-FULLST-024 found the actual cause: 5 leftover rows in the orphan `promotion_cours` table (PIT-010) for promotion id=4, with an active FK to `promotion(id)` that no current entity/repository maps to, so the FK violation bubbled up unhandled.

**How to apply:** When debugging an unexplained 403 on a write endpoint with an empty response body and no `GlobalExceptionHandler` mapping for the thrown exception, grep the backend log for `ERROR:` / `DataIntegrityViolation` around the request timestamp BEFORE assuming it's an authorization bug. If found, check `promotion_cours` and any other orphan tables left by entity renames under `ddl-auto=update` (see PIT-010) for rows referencing the entity being deleted.

**Counter-indications:** Does not apply once `promotion_cours` is dropped/cleaned in all environments and/or `GlobalExceptionHandler` gains a mapping for `DataIntegrityViolationException` -> 409/500.
