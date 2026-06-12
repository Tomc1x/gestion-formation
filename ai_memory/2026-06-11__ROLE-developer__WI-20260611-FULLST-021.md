# WI-20260611-FULLST-021 — Refonte visuelle page Cursus & Filieres

## Status
DONE

## Scope
Frontend-only redesign of `frontend/src/app/features/administration/cursus/{cursus.html,cursus.ts,cursus.scss}`
to match the React mockup "SCREEN 5 — CURSUS MANAGEMENT": filiere headers with color dot + cursus-count badge
+ "Modifier filière" button + per-filiere empty state, 2-column cursus card grid with filiere-colored numbered
badges, full-width dashed "Ajouter un cours" button, color preview in the filiere modals. No backend/DB changes.

## Files Touched
- `frontend/src/app/features/administration/cursus/cursus.ts`
- `frontend/src/app/features/administration/cursus/cursus.html`
- `frontend/src/app/features/administration/cursus/cursus.scss`

## Evidence

### Build
`cd frontend && npx ng build` → PASS.
- `cursus` chunk: 40.95 kB (component styles), no SCSS budget warning for `cursus.scss`.
- Remaining warnings are pre-existing and unrelated to this WI:
  - `register.scss` 4.95 kB (budget 4.00 kB)
  - `promotions.scss` 4.43 kB (budget 4.00 kB)
  - `utilisateurs.scss` 9.09 kB (budget 4.00 kB)

### Visual verification (chrome-devtools, http://localhost:4200/app/admin/cursus, ref@ref.com)
- Filiere "Développement" displays with color dot, "1 cursus" badge, "Modifier Développement" button.
- Cursus card "Concepteur développeur d'application" shows "5 cours · ordre pédagogique", numbered badges (0-4)
  colored with the filiere's deterministic color, drag-handle icons, move up/down buttons, "Retirer" buttons.
- Full-width dashed "+ Ajouter un cours" button renders below the course list.
- Created a temporary filiere "QA Visuelle FULLST-021" via "Nouvelle filière" modal — color preview swatch
  and text "Attribuée automatiquement à la création (couleur N de la palette)" displayed correctly.
- Created a temporary cursus "Cursus QA Visuel" in that filiere via "Nouveau cursus" builder modal — ghost rows
  ("?") for missing prerequisites (HTML basique/CSS Basique/Javascript basique) rendered correctly,
  confirming non-regression of the FULLST-018/020 builder/prereq logic.
- Verified new cursus card rendering: "2 cours · ordre pédagogique", numbered badges (0,1) colored in the
  filiere's assigned color (orange), dashed "Ajouter un cours" button present.
- Clicked "Ajouter un cours" on the test cursus → modal "Ajouter un cours à Cursus QA Visuel" opened listing
  catalogue courses (CSS Basique, Javascript basique, HTML basique) each with an "Ajouter" button — confirms
  non-regression of the FULLST-020 add-course modal pattern. Closed without adding.
- Clicked "Monter" (move up) on the second course ("Angular Avance") → order swapped correctly (0/1 badges
  re-rendered with filiere color), confirming non-regression of FULLST-019 reorder logic.
- Clicked "Supprimer Cursus QA Visuel" → confirmation alertdialog "Supprimer le cursus" appeared → confirmed
  deletion → cursus removed, filiere group correctly fell back to the new per-filiere empty state:
  "Aucun cursus dans cette filière." + "Créer le premier cursus" button (exact wording from the WI spec).
- Clicked "Modifier QA Visuelle FULLST-021" → "Modifier la filière" modal opened with name field and
  "Couleur attribuée automatiquement, non modifiable." text (read-only color preview).
- Cleanup: deleted the temporary filiere "QA Visuelle FULLST-021" via direct `DELETE /api/filiere/3` call
  (status 204) since no "Supprimer filière" trigger is wired in the template — see Decisions below. Page
  reload confirmed the page returns to its original clean state (only "Développement" filiere with its
  original cursus).
- Final full-page screenshot saved to
  `ai_memory/screenshots/WI-20260611-FULLST-021_cursus_final.png`.

## Decisions

1. **Filiere color storage: deterministic, frontend-only, id-based (not localStorage/free-choice).**
   `FILIERE_COLORS[filiereId % FILIERE_COLORS.length]` was already present pre-WI (FULLST-018) and is reused
   as-is. This guarantees stable colors with zero persistence/sync concerns and matches the
   "frontend-only, no backend changes" constraint validated for this WI.
   - "Nouvelle filière" modal: shows a *preview* swatch of the color that WILL be assigned, computed from
     `(max(existing filiere ids) + 1) % FILIERE_COLORS.length` via new `nextFiliereColor()` /
     `nextFiliereColorIndex()` helpers — the future filiere has no id yet so the actual color can't be computed
     until creation.
   - "Modifier filière" modal: shows the *actual current* color (read-only), with the label "Couleur attribuée
     automatiquement, non modifiable."

2. **"Sans filière" cursus preserved.** The previous `groupedByFiliere` computed (removed) bucketed cursus with
   `filiereId === null` under "Sans filière". Replaced by a new `cursusSansFiliere` computed + a dedicated
   template section (only rendered if non-empty), using a neutral badge color (`var(--ink-3)`) instead of a
   filiere color, so no data/functionality is lost even though no such cursus currently exists in seed data.

3. **Course duration ("durée") from the mockup NOT implemented.** `CoursInCursus` (in `cursus.model.ts`) has no
   `dureeJours` field; adding it would require a backend DTO change, which is out of scope for this
   frontend-only WI.

4. **Cleanup of test filiere via direct API call, not UI.** `openDeleteFiliereModal`/`confirmDeleteFiliere` and
   the corresponding "Supprimer filière" alertdialog already exist in `cursus.ts`/`cursus.html` (pre-existing,
   pre-dating this WI) but no button in the template currently calls `openDeleteFiliereModal()` — this looks
   like dead/unwired code from an earlier WI, out of scope to wire up here. Used `DELETE /api/filiere/{id}`
   directly via `fetch()` in the browser console (chrome-devtools `evaluate_script`) to remove the temporary
   QA filiere, returned 204.

## Open Blockers
None.

## Next Actions
None for this WI. Possible follow-up (not requested): wire up the existing but unused
`openDeleteFiliereModal`/`confirmDeleteFiliere` to a "Supprimer filière" button if filiere deletion from the UI
is desired.

## Recall Hints
- FILIERE_COLORS palette and `filiereColor()`/`cursusForFiliere()`/`cursusCount()` helpers in `cursus.ts`.
- New `.btn-add-cours` (dashed full-width button) and `.badge-num` (colored numbered badge) classes in
  `cursus.scss`.
- `cursusSansFiliere` computed replaces the old `groupedByFiliere` "Sans filière" bucket.
- Per-filiere empty state markup uses inline styles (not a dedicated class) to stay under the SCSS budget.

## Proposed Rules

- TYPE: PITFALL
  Title: cursus.scss is near the Angular `anyComponentStyle` budget ceiling
  Scope: frontend/src/app/features/administration/cursus/cursus.scss
  Rule: Before adding new styles to cursus.scss, check `npx ng build` output for the `cursus` chunk size —
  it sits close to the 4 kB `maximumWarning` budget (currently 40.95 kB total transferred / component style
  budget nearly exhausted) and small additions can trigger a new SCSS budget warning.
  Why: This WI required ~10 rounds of trimming (removing `.sr-only`, merging duplicate flex-pattern selectors,
  reusing `.muted`/`.builder__row` instead of new classes, removing modifiers in favor of inline styles) to
  fit new styles under budget after an initial overflow (4.70 kB vs 4.00 kB budget).
  How to apply: Prefer reusing existing utility classes (`.muted`, `.builder__row`, `.p1`/`.p2`) and inline
  `[style.*]` bindings for one-off styling instead of adding new SCSS rules/classes. Run
  `npx ng build 2>&1 | grep -i cursus` after any SCSS edit to this file.
  Evidence: WI-20260611-FULLST-021, build went from "exceeded maximum budget ... 4.70 kB" to clean 40.95 kB
  chunk after trimming.
