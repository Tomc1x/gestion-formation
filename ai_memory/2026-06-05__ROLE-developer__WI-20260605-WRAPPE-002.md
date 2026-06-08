# Developer Memory Note

## Work Item
WI-20260605-WRAPPE-002 — Header Angular : port du Topbar React

## Role
developer

## Status
DONE

## Scope
- Create `auth.constants.ts` with Role type, RoleMeta, UserProfile interfaces and mock data
- Extend `auth.service.ts` with currentRole, currentUser, currentRoleMeta signals + setRole() + logout reset
- Create `AvatarComponent` (avatar.ts, avatar.html, avatar.scss)
- Create `RoleBadgeComponent` (role-badge.ts, role-badge.html, role-badge.scss)
- Rewrite `HeaderComponent` (header.ts, header.html, header.scss)

## Files Touched
- `frontend/src/app/core/services/auth.constants.ts` — created
- `frontend/src/app/core/services/auth.service.ts` — modified
- `frontend/src/app/shared/components/avatar/avatar.ts` — created
- `frontend/src/app/shared/components/avatar/avatar.html` — created
- `frontend/src/app/shared/components/avatar/avatar.scss` — created
- `frontend/src/app/shared/components/role-badge/role-badge.ts` — created
- `frontend/src/app/shared/components/role-badge/role-badge.html` — created
- `frontend/src/app/shared/components/role-badge/role-badge.scss` — created
- `frontend/src/app/layouts/main-layout/header/header.ts` — rewritten
- `frontend/src/app/layouts/main-layout/header/header.html` — rewritten
- `frontend/src/app/layouts/main-layout/header/header.scss` — rewritten

## Evidence
```
Application bundle generation complete. [2.224 seconds]
Output location: dist/frontend
0 errors, 0 warnings
```
Build passes clean after removing unused LucideDynamicIcon import (added by mistake, removed immediately).

## Decisions
- `LucideDynamicIcon` was removed from header.ts imports — all icons are used directly as individual Lucide directives, no need for the dynamic component
- Avatar font size computed as `Math.round(size * 0.38)` to maintain proportional readability at all sizes
- `--red-bg` CSS variable exists in variables.scss as `$red-bg: #FDECEC`; used directly via `rgba()` fallback in danger hover to avoid potential missing var issues in compiled output
- `card` class on `.dropdown` references the global `.card` pattern for box styles; since no global `.card` rule exists in styles.scss, the border/radius/background are set inline in the dropdown selector
- Role badge uses `inline-block` not `inline-flex` — simpler for text-only badge, consistent with the React reference

## Open Blockers
None.

## Next Actions
- Visual QA: verify dropdown positioning on different screen sizes
- Wire up navigation in "Mon profil" and "Paramètres" buttons when routes exist

## Proposed Rules
- TYPE: PITFALL
  Title: LucideDynamicIcon unnecessary when using named Lucide directives
  Scope: Angular components using @lucide/angular
  Rule: Only import LucideDynamicIcon when the icon name is determined at runtime; for static icons use named imports (LucideBell, LucideSearch, etc.) directly as directives.
  Why: Including LucideDynamicIcon when not used in template triggers NG8113 warning and adds unnecessary bundle weight.
  How to apply: Check if any `<i-lucide [name]="...">` or `lucide-dynamic-icon` elements appear in the template before importing LucideDynamicIcon.
  Evidence: WI-20260605-WRAPPE-002, header.ts initial build warning NG8113
