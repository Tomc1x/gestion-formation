# WI-20260611-FULLST-036

## Work Item
WI-20260611-FULLST-036 — Confiner le scroll de l'application a la zone `<main>` (router-outlet), pas a toute la page.

## Role
developer

## Status
DONE

## Scope
- frontend/src/app/layouts/main-layout/main-layout.scss (rule `main`)
- Verification only (no changes needed): frontend/src/styles.scss, frontend/src/app/layouts/main-layout/sidebar/sidebar.scss, frontend/src/app/layouts/main-layout/header/header.scss

## Files Touched
- frontend/src/app/layouts/main-layout/main-layout.scss

## Evidence
- `npm run build` (frontend) succeeded: "Application bundle generation complete. [4.986 seconds]". Only pre-existing CSS budget warnings (utilisateurs.scss, promotions.scss, cursus.scss, register.scss) unrelated to this change.
- Checked frontend/src/styles.scss for `100vh`/`overflow`/`fadeUp`/`translateY`: WI-034 fix at lines 416-418 (`@keyframes fadeUp` uses `transform: none`, not `translateY(0)`) is untouched.
- Checked sidebar.scss: `height: 100vh` at lines 20 and 192 — these belong to the sidebar's own fixed column, no conflict with `main` scrolling. Sidebar already has its own `overflow-y: auto` at line 72 for its internal nav list.
- Checked header.scss: only `overflow: hidden`/`text-overflow: ellipsis` for text truncation, no `100vh` or layout-breaking rules.

## Decisions
- Added to `main` rule in main-layout.scss: `min-height: 0px`, `height: 0px`, `overflow-y: auto`. The `height: 0px` combined with `flex: 1 1 0%` forces the flex item to compute its height from the flex algorithm (not content), which together with `min-height: 0` allows `overflow-y: auto` to actually create a scroll container confined to `<main>` instead of the whole `.main-content`/page growing with content.
- `.main-content` already had `max-height: 100vh`, which combined with the new `main` rules keeps `app-header` fixed at top and `app-sidebar` (which has its own `height: 100vh`) fixed on the left, while only `<main>` scrolls.
- No changes needed to styles.scss, sidebar.scss, or header.scss — none of them had conflicting height/overflow rules, and the WI-034 fadeUp fix was confirmed intact.

## Open Blockers
None.

## Next Actions
- Optional: visual verification via chrome-devtools on a long-content page (e.g. /app/admin/cursus/:id or /app/admin/utilisateurs) to confirm sidebar/header stay fixed while main scrolls, and that WI-034 modals still work. Not performed in this session (build verification only).

## Recall Hints
- main-layout.scss `main` rule, flex scroll containment, WI-034 fadeUp keyframe interaction.

## Proposed Rules
None — this is a localized, project-specific CSS fix consistent with existing flex layout patterns.
