# WI-20260611-FULLST-024 — Cascade delete fixes for promotions/cours planning + cours catalogue

## Work Item
WI-20260611-FULLST-024

## Role
developer

## Status
DONE

## Scope
Fix FK violations on `DELETE /api/promotions/{id}` and `DELETE /api/cours/{id}` caused by
`InscriptionCours` and `CoursPlanifie` rows referencing the deleted entities (no JPA cascade).
Add `DELETE /api/promotions/{id}/planning/{coursPlanifieId}` endpoint + frontend trash icon
with confirmation modal. Clean up known debris promotion id=4 ("TEST CDA 2").

## Files Touched

### Backend
- `backend/src/main/java/fr/eni/gestionformation/repository/InscriptionCoursRepository.java`
  — added `findByCoursPlanifieIdIn(List<Long>)`
- `backend/src/main/java/fr/eni/gestionformation/repository/CoursPlanifieRepository.java`
  — added `findByCoursId(Long)`
- `backend/src/main/java/fr/eni/gestionformation/repository/CoursRepository.java`
  — added `findByPrerequisId(Long)` (HQL `JOIN c.prerequis p WHERE p.id = :prerequisId`)
- `backend/src/main/java/fr/eni/gestionformation/service/PromotionService.java`
  — injected `InscriptionCoursRepository`; `deleteById` now deletes InscriptionCours
    referencing the promotion's CoursPlanifie before deleting them; added
    `deletePlanning(promotionId, coursPlanifieId)` (validates ownership via
    `CoursPlanifieNotFoundException`, deletes referencing InscriptionCours then the
    CoursPlanifie); extracted private helper `deleteInscriptionsForPlanning(List<CoursPlanifie>)`
- `backend/src/main/java/fr/eni/gestionformation/controller/PromotionController.java`
  — added `DELETE /{id}/planning/{coursPlanifieId}` -> 204
- `backend/src/main/java/fr/eni/gestionformation/service/CoursService.java`
  — injected `CoursPlanifieRepository` + `InscriptionCoursRepository`; `deleteById` now:
    1. cleans `CursusCours` links (existing DEC-001 behaviour, unchanged)
    2. finds `CoursPlanifie` for this cours, deletes referencing `InscriptionCours`, then
       deletes those `CoursPlanifie`
    3. clears `cours.prerequis` (this cours' own prerequisites) and removes this cours
       from any other `Cours.prerequis` set that references it (bidirectional cleanup
       of `cours_prerequis` join table, same pattern as `setPrerequis`)
    4. deletes the `Cours`

### Backend tests
- `backend/src/test/java/fr/eni/gestionformation/service/CoursServiceTest.java`
  — added `@Mock CoursPlanifieRepository` and `@Mock InscriptionCoursRepository` (constructor
    now requires them; no test exercises `deleteById` directly so no behavior change needed)
- `backend/src/test/java/fr/eni/gestionformation/service/PromotionServiceTest.java`
  — added `@Mock InscriptionCoursRepository` for the same reason

### Frontend
- `frontend/src/app/core/adapters/promotion.adapter.ts` — added abstract
  `deletePlanning(promotionId, coursPlanifieId): Observable<void>`
- `frontend/src/app/core/adapters/promotion-http.adapter.ts` — implemented via
  `DELETE ${API}/${promotionId}/planning/${coursPlanifieId}`
- `frontend/src/app/core/adapters/promotion-mock.ts` — implemented, removes the entry
  from `planning` (404 if not found)
- `frontend/src/app/features/promotions/promotion-detail/cours-planifies/cours-planifies-tab.ts`
  — added `LucideTrash2` import, `deleteTarget`/`deleting`/`deleteError` signals,
    `openDeleteConfirm`/`closeDeleteConfirm`/`confirmDelete` methods
- `frontend/src/app/features/promotions/promotion-detail/cours-planifies/cours-planifies-tab.html`
  — added trash icon button per row + local confirmation modal (same modal-overlay/card
    pattern as the existing "Modifier la session" modal)
- `frontend/src/app/features/promotions/promotion-detail/cours-planifies/cours-planifies-tab.scss`
  — added `.btn-danger` class (uses `var(--red)`)

Note: the prompt mentioned `promotion-detail.html`/`.ts` for the trash icon, but the actual
"Cours planifiés" table lives in the child component
`promotion-detail/cours-planifies/cours-planifies-tab.{ts,html,scss}` — that's where the
edit (pencil) button already was, so the new delete button was added there for consistency.

## Evidence

### Backend compile + tests
- `./gradlew compileJava` -> success (no output = clean)
- `./gradlew test --tests "*CoursServiceTest*" --tests "*PromotionServiceTest*"` -> BUILD SUCCESSFUL
- `./gradlew test` (full suite) -> BUILD SUCCESSFUL

### Frontend build
- `npx ng build` -> Application bundle generation complete, no new errors. Pre-existing
  CSS budget warnings on cursus/utilisateurs/promotions/register.scss unchanged
  (not in touched files).

### Manual API verification (local backend on :8080, ref@ref.com / toto785971,
REFERENTE_ADMINISTRATIVE)
- `DELETE /api/promotions/9/planning/25` (promotion with 1 CoursPlanifie + 1
  InscriptionCours referencing it) -> 204; both `inscription_cours` row and
  `cours_planifie` row 25 removed.
- `DELETE /api/promotions/10` (promotion with CoursPlanifie 26 + InscriptionCours
  referencing it, planning non-empty) -> 204; InscriptionCours, CoursPlanifie and
  Promotion all removed.
- `DELETE /api/cours/2` (cours referenced by standalone CoursPlanifie 27 with an
  InscriptionCours) -> 204; InscriptionCours, CoursPlanifie 27 and Cours 2 all removed.
- Prerequis bidirectional cleanup: set cours 4's prerequis = [3] (cours_prerequis rows
  `(4,3)` and pre-existing `(6,3)`), then `DELETE /api/cours/3` -> 204; both
  `(4,3)` and `(6,3)` rows removed from `cours_prerequis`, only `(6,4)` and `(6,5)`
  remain.

### Promotion id=4 ("TEST CDA 2") debris cleanup
- `GET /api/promotions/4` confirmed: name "TEST CDA 2", 0 eleves, 1 CoursPlanifie
  (id=19, "CSS Basique") — matches documented test debris (REPO_STATE known_issues).
- First `DELETE /api/promotions/4` attempt still returned 403 even with the new code.
  Root cause investigation (see Decisions below) found an **orphan `promotion_cours`
  table** (PIT-010, leftover from the PromotionCours -> CoursPlanifie rename) still
  containing 5 rows with `promotion_id = 4` and an active FK to `promotion(id)`. This
  table is NOT used by any current entity/repository (CoursPlanifie maps to
  `cours_planifie`), so the FK violation on delete bubbled up as an uncaught
  `DataIntegrityViolationException`, which Spring's filter chain in this project's
  config surfaces as an empty-body 403 — matching exactly the documented "Bug
  additionnel" symptom from WI-20260611-FULLST-022.
  - Cleanup: `DELETE FROM promotion_cours WHERE promotion_id = 4;` (5 rows, local DB only)
  - Retried `DELETE /api/promotions/4` -> 204. `GET /api/promotions/4` -> 404. Confirmed
    `SELECT id, name FROM promotion` is now empty (only debris promotion existed).

### Frontend chrome-devtools verification
- ng serve already running on :4200 (own attempt to start a second instance failed,
  port already in use — used the existing one, which picks up changes via HMR/watch).
- Logged in as ref@ref.com / toto785971.
- Created temp promotion (id=11, "WI024 UI Test") + planned cours 4 ("Javascript
  basique") via API for the test.
- Navigated to `/app/admin/promotions/11`: "Cours planifiés (1)" tab shows the new
  trash icon button "Retirer le cours planifié Javascript basique" next to the pencil
  edit button.
- Clicked trash icon -> confirmation modal "Retirer le cours planifié" opens with
  expected text and Annuler/Retirer buttons.
- Clicked "Retirer" -> modal closes, tab updates to "Cours planifiés (0)" with the
  "Aucun cours planifié pour cette promotion." empty-row message — matches
  `DELETE /api/promotions/11/planning/28` -> 204 behaviour.
- Cleaned up: `DELETE /api/promotions/11` -> 204 (test promotion removed).

## Decisions

1. **Root cause of the documented `DELETE /api/promotions/{id}` 403**: not a security
   bug at all — it's an unhandled `DataIntegrityViolationException` from the orphan
   `promotion_cours` table (PIT-010), which Spring Security's filter chain in this
   project surfaces as an empty-body 403 instead of 500. This WI's fix (deleting
   InscriptionCours/CoursPlanifie before the promotion) does NOT touch `promotion_cours`
   because no current code reads/writes that table — it's pure leftover schema from the
   `ddl-auto=update` rename. Cleaning up promotion 4's 5 orphan rows resolved the issue
   for this specific promotion. **If any other promotion in any environment still has
   rows in `promotion_cours`, the same 403 will recur on `DELETE /api/promotions/{id}`**
   until either those rows are cleaned or the orphan table is dropped via a proper
   migration. This is a pre-existing data-hygiene issue, not something this WI's code
   change can fix generically (no entity maps to `promotion_cours` anymore, so no
   repository can be used to clean it programmatically without adding a native query).

2. **Endpoint location for the trash icon**: prompt referenced
   `promotion-detail.html`/`.ts`, but the "Cours planifiés (N)" table (with the existing
   pencil "Modifier la session" button) is implemented in the child component
   `cours-planifies-tab`. Added the trash icon + confirm modal there for consistency
   with the existing edit button and modal pattern (same `.modal-overlay`/`.modal`
   classes, same component).

3. **Confirmation modal**: no generic reusable confirm-modal component exists in
   `promotions` or shared modules (checked `cours-planifies-tab`,
   `promotion-detail`, `stagiaires-tab`, `plan-course-modal`). Built a small local
   modal in `cours-planifies-tab.html` reusing the `.modal-overlay`/`.modal`/`.modal__header`/
   `.modal__body`/`.modal__footer` classes already defined in
   `cours-planifies-tab.scss` (same as the "Modifier la session" modal). Added a new
   `.btn-danger` class scoped to this component's scss (no global `btn-danger` class
   existed; `utilisateurs.scss` has its own component-scoped `.btn-danger` with a
   different convention — `class="btn-danger"` without `btn`).

4. **CoursServiceTest / PromotionServiceTest**: added the two new repository mocks as
   `@Mock` fields (with `@SuppressWarnings("unused")` on CoursServiceTest's, matching
   existing convention there) purely so `@InjectMocks` constructor injection doesn't
   pass `null` for the new constructor params. No existing test exercises `deleteById`,
   so this is precautionary, not driven by a failing test.

## Open Blockers
None for this WI's scope.

## Next Actions
- Consider a follow-up WI/migration to either (a) add a native-query cleanup for any
  remaining `promotion_cours` rows across all environments, or (b) drop the orphan
  `promotion_cours` table entirely now that PIT-010's rename is fully complete and no
  entity maps to it. Until then, `DELETE /api/promotions/{id}` may still 403 for any
  promotion that has leftover `promotion_cours` rows from before the CoursPlanifie
  rename.

## Recall Hints
- "promotion delete 403", "promotion_cours orphan table", "FK violation
  promotion_cours fke4hvytgku0ixrlx66ynjclp65", "deletePlanning endpoint",
  "cours-planifies-tab trash icon", "cours_prerequis bidirectional cleanup on delete"

## Proposed Rules

- TYPE: PITFALL
  Title: Orphan `promotion_cours` table can cause `DELETE /api/promotions/{id}` to
    return an empty-body 403 instead of a real error
  Scope: backend/src/main/java/fr/eni/gestionformation/service/PromotionService.java
    (deleteById), DB table `promotion_cours` (PIT-010 predecessor of `cours_planifie`)
  Rule: If a `DELETE /api/promotions/{id}` (or similar) request returns an empty-body
    403 with standard Spring Security headers but no entry in
    GlobalExceptionHandler maps to 403 for the thrown exception, check the backend
    log for an uncaught `DataIntegrityViolationException` / FK violation BEFORE
    assuming it's an authorization bug — this project's filter chain surfaces such
    unhandled exceptions as 403, not 500.
  Why: WI-20260611-FULLST-022 documented this as an unidentified "Bug additionnel"
    (403 on promotion delete) and left it unresolved. WI-20260611-FULLST-024 found
    the actual cause: 5 leftover rows in the orphan `promotion_cours` table
    (PIT-010) for promotion id=4, with an active FK to `promotion(id)` that no
    current entity/repository can clean up.
  How to apply: When debugging unexplained 403s on write endpoints with empty
    response bodies, grep the backend log for `ERROR:` / `DataIntegrityViolation`
    around the request timestamp first. If found, check `promotion_cours` (and any
    other orphan tables left by entity renames under `ddl-auto=update`, see PIT-010)
    for rows referencing the entity being deleted.
  Evidence: ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-024.md
    (backend log: "ERROR: update or delete on table \"promotion\" violates foreign
    key constraint \"fke4hvytgku0ixrlx66ynjclp65\" on table \"promotion_cours\"")

- TYPE: CONVENTION
  Title: Manual cascade-delete pattern for required (`optional=false`,
    `nullable=false`) `@ManyToOne` FKs without JPA cascade
  Scope: backend/src/main/java/fr/eni/gestionformation/service/*.java (any
    `deleteById` whose entity is the target of a non-cascading required FK)
  Rule: Before deleting an entity X that other entities Y reference via a required
    `@ManyToOne` (no `cascade`/`orphanRemoval`), find all Y rows referencing X via a
    new `findByXId(...)` repository method, delete the deepest-dependent rows first
    (e.g. `InscriptionCours` before `CoursPlanifie` before `Cours`/`Promotion`), in
    the same `@Transactional` service method, before the final `repository.delete(x)`.
  Why: `InscriptionCours.coursPlanifie` and `CoursPlanifie.cours`/`.promotion` are all
    `optional=false`/`nullable=false` with no cascade, so naive deletion of
    `Cours`/`Promotion`/`CoursPlanifie` throws a raw `DataIntegrityViolationException`
    (FK violation), surfaced as an unhelpful 403/500.
  How to apply: see `PromotionService.deleteInscriptionsForPlanning` and
    `CoursService.deleteById` (steps 2-3) in this WI for the reference
    implementation. Order matters: InscriptionCours -> CoursPlanifie -> parent.
  Evidence: backend/src/main/java/fr/eni/gestionformation/service/PromotionService.java,
    backend/src/main/java/fr/eni/gestionformation/service/CoursService.java
