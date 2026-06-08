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
