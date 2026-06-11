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
