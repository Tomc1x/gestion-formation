# Work Item: WI-20260605-WRAPPE-001

## Role
developer

## Status
DONE

## Scope
Sidebar mobile drawer (hamburger ≤ 900px) — Angular 20+, signals, OnPush, no ngClass/ngStyle.

## Files Touched
1. `frontend/src/app/layouts/main-layout/sidebar/sidebar.service.ts` — CREATED
2. `frontend/src/app/layouts/main-layout/sidebar/sidebar.ts` — MODIFIED
3. `frontend/src/app/layouts/main-layout/sidebar/sidebar.html` — MODIFIED
4. `frontend/src/app/layouts/main-layout/sidebar/sidebar.scss` — MODIFIED (bug fix + drawer styles)
5. `frontend/src/app/layouts/main-layout/header/header.ts` — MODIFIED
6. `frontend/src/app/layouts/main-layout/header/header.html` — MODIFIED
7. `frontend/src/app/layouts/main-layout/header/header.scss` — MODIFIED (created, was empty)

## Evidence
Build result:
```
Application bundle generation complete. [2.100 seconds]
Output location: frontend/dist/frontend
No errors, no warnings.
```

## Decisions

### SidebarService
- `providedIn: 'root'` singleton with `isOpen = signal(false)`, `toggle()`, `close()`.
- Injected in both `SidebarComponent` and `HeaderComponent` — shared state without prop drilling.

### LucideIconInput type
- Routes array typed as `{ path: string; label: string; icon: LucideIconInput }[]`.
- `LucideCalendar` and `LucideUsers` removed from `imports[]` of the decorator (unused as template directives — only used as data values). `LucideGraduationCap` and `LucideLifeBuoy` remain in `imports[]` because they are used as attribute directives in the template.
- Original code used `any`; replaced with the correct `LucideIconInput` type from `@lucide/angular`.

### Bug fix — broken media query
- Original `sidebar.scss` had `@media (max-width: 900px)` block where `.sidebar`, `.logoframe`, `nav` were redeclared at root level (outside `.sidebar`) instead of nested. This meant the cascade never applied correctly.
- Rewrote the entire file with proper nesting; mobile block only overrides `position/transform` and shows the backdrop.

### Backdrop visibility
- Used `.sidebar-backdrop.visible` class binding (via `[class.visible]="sidebarService.isOpen()"`) + CSS opacity/pointer-events transition for smooth fade-in/out.
- `display: none` on desktop to ensure it never renders outside mobile viewport.

### Hamburger button
- `display: none` by default; `display: flex` inside `@media (max-width: 900px)`.
- ARIA: `aria-label="Ouvrir la navigation"`, `[attr.aria-expanded]="sidebarService.isOpen()"`.
- `focus-visible` outline for WCAG AA keyboard navigation.

### Nav item click closes drawer
- Added `(click)="sidebarService.close()"` on nav items so navigating closes the drawer automatically on mobile.

## Open Blockers
None.

## Next Actions
- Visual verification in browser at ≤ 900px viewport (resize or DevTools).
- Check that desktop layout is unchanged (sidebar sticky, no backdrop visible).
- Optionally: add keyboard trap (Escape key closes drawer) for full WCAG compliance.

## Recall Hints
- `LucideIconInput` is the correct union type for lucide icon references: `LucideIcon | LucideIconData | string`.
- In Angular v20+, icons used only as data (not as attribute directives in template) must NOT be in `imports[]`.
- Media query nesting in Angular SCSS component files must be self-contained — selectors inside `@media` blocks must be fully qualified or use SCSS nesting.

## Proposed Rules

- TYPE: PITFALL
  Title: Lucide Angular — imports[] vs data usage
  Scope: Angular components using @lucide/angular icon components
  Rule: Only include a Lucide icon in the component `imports[]` array if it appears as an attribute directive (e.g., `lucideMenu`) in the template; icons used solely as data values in a TypeScript array must NOT be in `imports[]`.
  Why: Angular compiler emits NG8113 warnings for unused imports, and the build treats them as errors in strict mode.
  How to apply: Check each icon in `imports[]` — if it only appears in a `.ts` data array and never as `lucide*` attribute in the HTML template, remove it from `imports[]`.
  Evidence: WI-20260605-WRAPPE-001, sidebar.ts

- TYPE: PITFALL
  Title: SCSS media query nesting — component styles
  Scope: Angular component SCSS files using BEM or nested selectors
  Rule: Inside a `@media` block in Angular component SCSS, all selectors must be properly nested; redeclaring top-level selectors (e.g., `.sidebar { }`) at root level inside `@media` is silently ignored because Angular's view encapsulation adds attribute selectors that break the match.
  Why: The original sidebar had a broken `@media (max-width: 900px)` block where `.sidebar` was at root level inside the media query, making the mobile overrides completely ineffective.
  How to apply: Always write mobile overrides as `@media (...) { .parent { .child { } } }` with full nesting, or use `@media` inside the existing nested block.
  Evidence: WI-20260605-WRAPPE-001, sidebar.scss
