# WI-20260612-FRONTE-005 — Modals non centrés (stagiaires-tab + utilisateurs)

## Status
DONE

## Scope
Fix global CSS root cause causing `.modal-overlay` (position: fixed; inset: 0) to be positioned
relative to `.page` instead of the viewport, breaking centering of modals.

## Root Cause (confirmed with evidence)
File: `frontend/src/styles.scss`, `@keyframes fadeUp` (used by `.anim-up`).

The existing code had:
```scss
@keyframes fadeUp {
  from { transform: translateY(10px); }
  to { transform: none; }
}
```
with a comment claiming `transform: none` in the final keyframe avoids creating a CSS containing
block on `.page` (via `animation-fill-mode: forwards`).

**This assumption is incorrect.** Verified via isolated repro page (`file://.../repro.html`)
replicating `.page.anim-up` > tall content > `.modal-overlay`:

- After the `.35s` `fadeUp` animation finished and settled (forwards fill), `getComputedStyle(.page).transform`
  returned `"matrix(1, 0, 0, 1, 0, 0)"`, NOT the keyword `"none"`.
- Per the CSS spec, ANY computed `transform` value other than the keyword `none` (including the
  identity matrix) establishes a containing block for `position: fixed`/`absolute` descendants.
- Result: `.modal-overlay` (`position: fixed; inset: 0`) was positioned relative to `.page`'s box
  (`getBoundingClientRect()` of overlay = top:0, left:0, width:1265, height:1500 — matching `.page`'s
  tall content height, not the 800px viewport height).

This affects any page using `.anim-up` on its root `.page` element that contains a `.modal-overlay`
descendant — e.g. `stagiaires-tab.html` modal #1 (`.page.anim-up` on `promotion-detail.html`).

For modal #2 (`utilisateurs.html`), the root `.page` div does NOT carry `.anim-up` (confirmed via
grep and live browser inspection: `getComputedStyle` chain from `.modal-overlay` up to `<html>`
showed `transform: none` / `contain: none` / `filter: none` on every ancestor, and the overlay's
`getBoundingClientRect()` was correctly `{top:0, left:0, width:1280, height:800}` = full viewport).
Live test (logged in as admin@admin.com, opened "Modifier le profil" modal) confirmed it renders
correctly centered already. No separate cause found for modal #2 at the current code state; the
fadeUp fix is applied globally as instructed and does not regress it.

## Fix
File: `frontend/src/styles.scss` (lines ~415-425)

Replaced the `transform`-based `fadeUp` keyframes with a `margin-top`-based animation, which
produces the same visual "fade up" motion without ever setting a non-`none` `transform` on `.page`
(so no containing block is ever created):

```scss
@keyframes fadeUp {
  from { margin-top: 10px; }
  to { margin-top: 0; }
}
```

Updated the inline comment to document the real mechanism (computed `matrix(1,0,0,1,0,0)` !=
keyword `none`, containing block rule) so this isn't reintroduced.

## Verification
1. Repro page BEFORE fix: `.modal-overlay` rect = `{top:0, left:0, width:1265, height:1500}`,
   `.page` computed transform = `matrix(1, 0, 0, 1, 0, 0)`. Confirms bug.
2. Applied fix (margin-top keyframes).
3. Repro page AFTER fix: `.modal-overlay` rect = `{top:0, left:0, width:1265, height:800}` (= full
   viewport height), `.page` computed transform = `"none"`. Bug fixed.
4. `cd frontend && npm run build` — PASSED (only pre-existing SCSS budget warnings on unrelated
   components: dashboard, promotions, utilisateurs, register, mon-calendrier, cursus — all
   pre-existing, not caused by this change).
5. Live browser check (`ng serve` on :4300, backend on default port, logged in as
   admin@admin.com / Admin123):
   - Dashboard page (`.anim-up` on `.page`) reloaded after the CSS change — renders correctly,
     fade-up effect still visually present (cards/lists render normally, no layout shift).
   - Utilisateurs page → "Modifier le profil" modal opens centered on screen (screenshot taken).
6. Could NOT live-test modal #1 (`stagiaires-tab.html` on `promotion-detail.html`) in the browser
   because that route requires role `REF` (roleGuard(['REF'])) and only `ADMINISTRATEUR`
   credentials (admin@admin.com/Admin123) were available/known. The fix was validated via the
   isolated repro page that exactly reproduces `.page.anim-up` + tall content + `.modal-overlay`,
   which is the precise structural pattern of `promotion-detail.html` > `stagiaires-tab.html`.

## Files Touched
- `frontend/src/styles.scss` (only file modified — `@keyframes fadeUp`, lines ~415-426)

## Decisions
- Did not touch `utilisateurs.html`/`.scss` or `stagiaires-tab.html`/`.scss` — the `.modal-overlay`/
  `.modal` CSS in both is already correct (`position: fixed; inset: 0; display:flex; align-items:
  center; justify-content: center`). The bug was 100% in the shared `fadeUp` keyframes in
  `styles.scss`, a global file.
- Kept `.anim-up` class and its usages unchanged across all 8 files that reference it
  (dashboard, cursus, cursus-detail, promotion-detail, cours, promotions (admin), inscrits,
  styles.scss itself) — only the keyframe definition changed, so all `.anim-up` usages benefit
  automatically without per-file edits.

## Open Blockers
None. Work item considered complete from a code-correctness standpoint. Recommend a manual visual
check by a REF-role user on `promotion-detail.html` → Stagiaires tab → "Ajouter un élève" modal to
close the loop, since it could not be live-verified due to missing REF credentials.

## Next Actions
- (Optional) Manager/QA: log in as a REF user (e.g. ref@ref.com or ref@eni.fr — passwords unknown
  to this session) and confirm the "Ajouter un élève" modal on promotion-detail/Stagiaires now
  centers correctly on screen.

## Recall Hints
- `fadeUp` keyframes, `.anim-up`, `.modal-overlay`, containing block, `position: fixed`,
  `animation-fill-mode: forwards`, `transform: none` vs `matrix(1,0,0,1,0,0)`.

## Proposed Rules
- TYPE: PITFALL
  Title: `transform: none` in a forwards-filled CSS animation does NOT remove the containing block
  Scope: frontend/src/styles.scss and any SCSS using `animation-fill-mode: forwards` with a
    `transform` property, especially ancestors of `position: fixed`/`absolute` elements (modals,
    overlays, tooltips, dropdowns).
  Rule: Never animate `transform` (even ending at `transform: none`) on an ancestor of a
    `position: fixed` overlay/modal when using `animation-fill-mode: forwards` — the browser keeps
    a computed `matrix(1,0,0,1,0,0)` (not the keyword `none`) after the animation, which establishes
    a CSS containing block and breaks `position: fixed` for all descendants (they become positioned
    relative to that ancestor instead of the viewport).
  Why: Caused WI-20260612-FRONTE-005 — modals on pages with `.anim-up` (`.page.anim-up`) were not
    centered on the viewport because `.modal-overlay` (position: fixed; inset: 0) was positioned
    relative to `.page` instead of the screen.
  How to apply: For entrance animations on page/section containers that may host overlays/modals
    as descendants, animate non-transform properties (`opacity`, `margin`, `padding`, `max-height`)
    instead of `transform`/`translate`. If `transform` is unavoidable, render the modal/overlay via
    Angular `cdkPortal`/a top-level outlet outside the animated ancestor's DOM subtree.
  Evidence: `frontend/src/styles.scss` `@keyframes fadeUp` (before/after this WI); isolated repro
    page measurements documented above.
