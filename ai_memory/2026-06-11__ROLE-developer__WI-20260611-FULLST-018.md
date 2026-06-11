# WI-20260611-FULLST-018 (+ FULLST-016 scope B)

## Work Item
WI-20260611-FULLST-018 — Frontend - Refonte liste Promotions (cartes) + Page Cursus & Filieres (FULLST-016)

## Role
developer

## Status
READY_FOR_REVIEW (build PASS, visual verification not done — see Open Blockers)

## Scope

### Scope A — Promotions list -> card grid
- `frontend/src/app/features/administration/promotions/promotions.ts`
- `frontend/src/app/features/administration/promotions/promotions.html`
- `frontend/src/app/features/administration/promotions/promotions.scss`

### Scope B — Cursus & Filieres (FULLST-016)
- `frontend/src/app/features/administration/cursus/cursus.ts`
- `frontend/src/app/features/administration/cursus/cursus.html`
- `frontend/src/app/features/administration/cursus/cursus.scss`

## Files Touched
- `frontend/src/app/features/administration/promotions/promotions.ts`
- `frontend/src/app/features/administration/promotions/promotions.html`
- `frontend/src/app/features/administration/promotions/promotions.scss`
- `frontend/src/app/features/administration/cursus/cursus.ts`
- `frontend/src/app/features/administration/cursus/cursus.html`
- `frontend/src/app/features/administration/cursus/cursus.scss`

## Implementation summary

### Promotions (Scope A)
- Replaced the `<table>` with a 3-column card grid (`.promo-grid` / `.promo-card`), matching
  "Screens A": colored band on top (filiere color), `StatusPill`-equivalent (`.status-pill`)
  for the derived statut, filiere name + promotion title, cursus line with `book-open-check`
  icon, period via `fmtRange()`, "{n} stagiaires" badge, "Détails →" link. Whole card is
  clickable -> `openPromotion()` -> `/app/admin/promotions/:id`.
- Added a filter bar (`.filter-bar`, card) above the grid: text search (nom promo / nom
  cursus, accent-naive `.toLowerCase().includes`), filiere `<select>`, year `<select>`
  (years derived from `promotion.dateDebut`).
- `EmptyState` equivalent inline (`.empty-state`) shown when `filteredCards()` is empty,
  with a "Nouvelle promotion" CTA (same as design).
- Kept existing "Nouvelle promotion" create modal and the delete confirmation modal
  unchanged in markup/logic. **Delete trigger relocated**: was a `<button>` (trash icon) in
  a dedicated table actions column; now a small `btn-ghost btn-icon` trash button inside
  `.promo-card__footer-actions`, with `(click)="$event.stopPropagation(); openDeleteModal(...)"`
  to avoid triggering card navigation. Documented here per task instructions.

### Model gaps vs design (documented, NOT invented on backend)
- **`Filiere.couleur` does not exist** (`frontend/src/app/core/models/cursus.model.ts`,
  `Filiere { id, name }`). Reused the existing `FILIERE_COLORS` deterministic-palette
  pattern already established in `cursus.ts` (`FILIERE_COLORS[filiere.id % FILIERE_COLORS.length]`)
  — purely visual, frontend-only, no backend change. Same array duplicated locally in
  `promotions.ts` (consistent with the project's existing tolerance for small duplication,
  cf. PIT-005 — extracting a shared constant was judged out of scope for this WI).
- **`Promotion.statut` does not exist** and **`Promotion.dateFin` does not exist**.
  - `dateFin` for the period display is derived as
    `max(planning[].dateFin)` (the last `CoursPlanifie.dateFin`), or `null` if
    `planning` is empty (then `fmtRange` shows only the start date).
  - `statut` is derived client-side via `computeStatut()`:
    `dateDebut > today` -> `'a-venir'` ("À venir"), else if `dateFin && dateFin < today`
    -> `'terminee'` ("Terminée"), else `'en-cours'` ("En cours"). This is a heuristic,
    not a persisted business status — flagged here in case the backend later introduces
    a real `Promotion.statut` enum (would then replace this computed value).
- `fmtRange` reproduces the design's helper exactly:
  `fmtDate(debut, {day:'2-digit', month:'short'}) + ' → ' + fmtDate(fin, {day:'2-digit', month:'short', year:'numeric'})`,
  locale `fr-FR`, only the start date shown if `dateFin` is null.
- No shared `StatusPill`/`Search`/`FilterSelect`/`EmptyState` components existed in
  `frontend/src/app/shared/components/` — built minimal local SCSS/markup equivalents
  inline in `promotions.html`/`.scss` (`.status-pill`, `.filter-bar`, `.empty-state`)
  rather than introducing new shared components, to stay within a single-module change.

### Cursus & Filieres (Scope B / FULLST-016)
The existing `cursus.ts`/`.html` (from WI-20260610-BACKEN-007) already implemented most of
"Screens B": `groupedByFiliere`, filiere CRUD modals, "Nouveau cursus" modal with
`builderRows` (ordered list + ghost rows for missing prerequisites + `fixOrder()` for
misordered prerequisites + monter/descendre/retirer). The only missing piece per FULLST-018
scope was **drag&drop reordering of an existing cursus's course list (`CursusCard`)**,
persisted via the existing `BaseCursusAdapter.reorder(cursusId, coursIds)`
(WI-20260610-BACKEN-007, confirmed wired end-to-end: `HttpCursusAdapter.reorder` ->
`PUT /api/cursus/{id}/cours/reorder`).

Added to the `cours-ordered-list` (`<li>` per course in a cursus card):
- `draggable="true"` + `dragstart`/`dragend`/`dragover`/`drop` handlers
  (`onDragStart`, `onDragEnd`, `onDropOnCours`) — reorders the `coursIds` array locally
  then calls `cursusAdapter.reorder(cursus.id, reordered)`, updating `cursusList` signal
  on success or setting `formError` on failure.
- A `grip-vertical` Lucide icon as a visual drag handle (`.cours-ordered-list__grip`).
- **Keyboard/non-pointer alternative (accessibility decision)**: added "Monter"/"Descendre"
  icon buttons (`lucideArrowUp`/`lucideArrowDown`, same as already used in the "Nouveau
  cursus" modal builder) per row, calling `moveCoursInCursus(cursus, coursId, ±1)`, which
  computes the same swapped `coursIds` array and calls the same `persistReorder()`. HTML5
  drag&drop has no native keyboard equivalent, so this satisfies the task's requirement
  for "une alternative clavier minimale" without needing a more complex ARIA
  drag-and-drop pattern.
- "Nouvelle filière" / "Nouveau cursus" modals, prerequisite ghost-row detection, and
  `fixOrder()` were already present and unchanged — confirmed they cover the FULLST-016
  scope B requirements (palette de couleurs for filiere creation was NOT present in the
  existing "Nouvelle filière" modal — see Open Blockers below, this is the one piece of
  Screens B not yet implemented).

## Verification
- `cd frontend && npx ng build` -> **PASS** (no errors). Output:
  - `chunk promotions: 22.19 kB raw / 5.81 kB transfer`
  - `chunk cursus: 28.30 kB raw / 6.58 kB transfer`
  - 3 pre-existing/new SCSS budget **warnings** (not errors, `maximumError: 12kB` not hit):
    - `register.scss` 4.95kB (pre-existing)
    - `utilisateurs.scss` 9.09kB (pre-existing, PIT-005)
    - `promotions.scss` 4.43kB (NEW, +428 bytes over the 4kB **warning** threshold —
      well under the 12kB error threshold, no action taken)
- Visual verification via chrome-devtools: **NOT COMPLETED**. `ng serve --port 4202`
  started cleanly (bundle generation complete, no errors). However login on
  `http://localhost:4202/login` with `admin@admin.com` / `admin` returned
  `401 Unauthorized` from the backend (`POST /api/auth/login`, confirmed via curl,
  backend reachable on :8080 and returns 200 on `GET /api/filiere`). I do not have
  valid credentials for this session (per user's "no demo accounts" policy, no seeded
  test account is documented in memory that I could find). Did not attempt to create a
  user via direct DB/API mutation as that is out of scope and risks interfering with
  concurrent agents (PIT-011).

## Decisions
1. Reused `FILIERE_COLORS` deterministic palette pattern (frontend-only) instead of a new
   `Filiere.couleur` backend field — see Model gaps section.
2. Derived `Promotion.statut` and period end-date client-side from `dateDebut` +
   `planning[].dateFin` — no backend change.
3. Delete-promotion trigger moved from a table action column to a small ghost icon button
   on the card footer with `stopPropagation`.
4. No new shared components (`StatusPill`/`Search`/`FilterSelect`/`EmptyState`) — built
   minimal local equivalents to keep the change scoped to 2 modules.
5. CursusCard drag&drop: kept the existing duplicated-pattern style (no new shared
   drag&drop directive/component) and added monter/descendre buttons as the keyboard
   alternative, reusing `cursusAdapter.reorder()`.

## Open Blockers
- Visual chrome-devtools verification not done (no working credentials for `ng serve`
  session). Recommend: provide a valid ADMIN/REF test account, or confirm
  `admin@admin.com` password, so a follow-up verification pass can run
  `/app/admin/promotions` and `/app/admin/cursus`.
- "Nouvelle filière" modal does not yet have a color palette picker (Screens B
  `NewFiliereModal` shows 8 clickable `FILIERE_COLORS` swatches with hex preview). Since
  `Filiere.couleur` doesn't exist on the backend (see Model gaps), a palette picker would
  have no field to bind to — out of scope here. If `Filiere.couleur` is added to the
  backend in a future WI, the palette picker should be added to `cursus.ts`'s
  `filiereForm`/`editFiliereForm` and `FILIERE_COLORS` should move to a shared constant
  consumed by both `cursus.ts` and `promotions.ts`.

## Next Actions
- Manager/QA: provide test credentials and run chrome-devtools visual pass on
  `/app/admin/promotions` (card grid, filters, delete button) and `/app/admin/cursus`
  (drag&drop reorder, monter/descendre buttons, existing modals).
- If `Filiere.couleur` is ever added: extract `FILIERE_COLORS` to a shared constant and
  wire the "Nouvelle/Modifier filière" modals with a color picker (see Open Blockers).

## Recall Hints
- Card grid + filter bar: `frontend/src/app/features/administration/promotions/promotions.{ts,html,scss}`
- CursusCard drag&drop + keyboard alternative: `frontend/src/app/features/administration/cursus/cursus.{ts,html,scss}`,
  search for `onDragStart`/`onDropOnCours`/`moveCoursInCursus`/`persistReorder`
- `FILIERE_COLORS` duplicated in both `promotions.ts` and `cursus.ts`

## Proposed Rules

- TYPE: PITFALL
  Title: Frontend models lack `Filiere.couleur` / `Promotion.statut` / `Promotion.dateFin` assumed by React design references
  Scope: frontend/src/app/core/models/cursus.model.ts, frontend/src/app/core/models/promotion.model.ts, any future WI implementing a provided React design for Promotions/Cursus
  Rule: Before implementing a UI design that references `filiere.couleur`, `promotion.statut`, or `promotion.dateFin`, check the actual frontend model — these fields do not exist; use the `FILIERE_COLORS` deterministic-palette pattern (frontend-only) for color and derive statut/dateFin client-side from `dateDebut`/`planning[].dateFin` instead of inventing new backend fields.
  Why: WI-20260611-FULLST-018 design references ("Screens A"/"Screens B") assumed these fields exist; implementing them as real backend fields would have been a much larger, uncoordinated cross-module change.
  How to apply: grep `cursus.model.ts`/`promotion.model.ts` for the field before using it in a template; if missing, derive client-side and document the gap in the role memory note (as done here), or raise a new backend WI if a persisted field is genuinely needed.
  Evidence: ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-018.md (this file), frontend/src/app/core/models/cursus.model.ts, frontend/src/app/core/models/promotion.model.ts
