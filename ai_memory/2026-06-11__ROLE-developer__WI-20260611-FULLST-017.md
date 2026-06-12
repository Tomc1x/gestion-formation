# WI-20260611-FULLST-017 — Developer memory note

## Status
READY_FOR_REVIEW

## Scope
Backend: new `POST /api/promotions/{id}/planning` endpoint to create a CoursPlanifie within
a promotion. Frontend: re-wire the "Planifier un cours" modal (creation mode) to call it.

## Files Touched

### Backend
- `backend/src/main/java/fr/eni/gestionformation/dto/PlanningCreateRequest.java` (new)
  — fields: coursId, dateDebut, dateFin, formateurId?, salle?, force (boolean, accepted
  but not yet used for any blocking check, consistent with FULLST-012 hypothesis).
- `backend/src/main/java/fr/eni/gestionformation/service/PromotionService.java`
  — added `createPlanning(promotionId, PlanningCreateRequest, warnings)`:
    - loads Promotion (404 via PromotionNotFoundException) and Cours (404 via
      CoursNotFoundException, new import + new field `coursRepository`)
    - computes `ordre` = max(existing ordre) + 1 (or 1 if planning empty)
    - sets statut = PLANIFIE
    - if formateurId provided, validates Role.FORMATEUR (same IllegalArgumentException
      as updatePlanning)
    - reuses formateur-conflict detection extracted into new private method
      `detecterConflitsFormateur(promotionId, coursPlanifie, warnings)` — refactored out
      of `updatePlanning` (same logic, now shared, no behavior change for updatePlanning).
- `backend/src/main/java/fr/eni/gestionformation/controller/PromotionController.java`
  — new `POST /{id}/planning` mapped to `createPlanning`, returns CoursPlanifieResponse
  (same `toCoursPlanifieResponse` helper as update).

### Backend tests
- `backend/src/test/java/fr/eni/gestionformation/service/PromotionServiceTest.java`
  — added `coursRepository` mock field (required by new PromotionService constructor
  param). New tests:
  - `createPlanning_promotionEtCoursExistants_creeUnCoursPlanifie` — verifies ordre
    increment, statut PLANIFIE, promotion/cours linkage, no warnings.
  - `createPlanning_coursInexistant_lanceCoursNotFound`.
- `backend/src/test/java/fr/eni/gestionformation/controller/PromotionControllerSecurityTest.java`
  — added 2 tests: `createPlanning_AvecRoleReferenteAdministrative_Retourne200` (POST
  /api/promotions/3/planning -> 200 for REFERENTE_ADMINISTRATIVE) and
  `createPlanning_AvecRoleEtudiant_Retourne403`.

### Frontend
- `frontend/src/app/core/models/promotion.model.ts` — new `PlanningCreateRequest`
  interface (coursId, dateDebut, dateFin, formateurId?, salle?, force?).
- `frontend/src/app/core/adapters/promotion.adapter.ts` — new abstract
  `createPlanning(promotionId, req): Observable<PromotionCours>`.
- `frontend/src/app/core/adapters/promotion-http.adapter.ts` — implements
  `createPlanning` -> `POST /api/promotions/{id}/planning`.
- `frontend/src/app/core/adapters/promotion-mock.ts` — mock implementation of
  `createPlanning` (coursNom set to placeholder `Cours #{id}` since the mock has no
  cours catalog).
- `frontend/src/app/features/promotions/promotion-detail/promotion-detail.ts` —
  `onPlanCourseSaved` now branches: if `editTarget()` is set -> existing
  `updatePlanning` flow (unchanged); else -> calls `createPlanning` and appends the
  returned PromotionCours to `promotion().planning`.
- `frontend/src/app/features/promotions/promotion-detail/plan-course-modal/plan-course-modal.html`
  — removed the "endpoint à venir / enregistrement désactivé" info banner (obsolete);
  submit button now always reads `Enregistrer` (edit) / `Planifier` (create) and is no
  longer `[disabled]`.

## Security
No SecurityConfig change needed — `/api/promotions/**` non-GET already
`hasRole("REFERENTE_ADMINISTRATIVE")`, covers the new POST `/{id}/planning`.

## Verifications

- `./gradlew test` -> BUILD SUCCESSFUL, all PromotionServiceTest (19 tests, incl. 2 new)
  and PromotionControllerSecurityTest (6 tests, incl. 2 new) pass, no failures/errors
  across the full suite (`grep -L 'failures="0" errors="0"' build/test-results/test/*.xml`
  returned nothing).
- `npx ng build` -> PASS (only pre-existing SCSS budget warnings on
  utilisateurs.scss / promotions.scss / register.scss, unrelated).
- chrome-devtools end-to-end: backend rebuilt + restarted on :8080 (old jar pre-dated
  this WI's changes, killed PID 33916, relaunched via
  `./gradlew bootRun --args='--spring.profiles.active=local'`). Created a clean test
  promotion (id=6, "TEST FULLST-017", cursusId=1) via curl as REF — promotion 3 remains
  corrupted per FULLST-019 note and was NOT touched. Logged in as
  ref@ref.com/toto785971, opened /app/admin/promotions/6, clicked "Planifier un cours"
  (creation mode, no banner), selected cours "Framework", dates 02/11/2026-06/11/2026,
  clicked "Planifier" -> modal closed, planning count went 6/5 -> 7/5, new row
  "Framework PLANIFIE 02/11/2026 – 06/11/2026" appeared in the table. Test promotion 6
  deleted afterwards (`DELETE /api/promotions/6` -> 204).

## Decisions
- Ordre assignment for created CoursPlanifie = `max(existing ordre) + 1` (or 1 if no
  existing planning entries) — simplest scheme consistent with how
  PlanificationService assigns ordre sequentially; no attempt to insert mid-sequence.
- `force` field accepted in PlanningCreateRequest/payload but not used for any blocking
  check, mirroring the existing FULLST-012 hypothesis documented in
  plan-course-modal.ts (prerequisite warnings remain non-blocking, frontend-only).
- Refactored the formateur-conflict-detection block out of `updatePlanning` into
  `detecterConflitsFormateur` private helper, reused by `createPlanning`. Pure
  extraction, no behavior change (existing updatePlanning tests still pass unchanged).

## Open Blockers
None.

## Next Actions
- None required for this WI. Promotion id=3 ("TEST CDA") remains corrupted from
  FULLST-019 — out of scope here per WI instructions (point 4), not touched.

## Recall Hints
- New endpoint: `POST /api/promotions/{id}/planning` body
  `{coursId, dateDebut, dateFin, formateurId?, salle?, force?}` -> CoursPlanifieResponse
  (with `warnings: string[]` for non-blocking formateur conflicts).
- Shared conflict-detection helper: `PromotionService.detecterConflitsFormateur`.

## Proposed Rules
None — this WI follows existing conventions (DTO/service/controller pattern,
WebMvcTest + SecurityMockMvcRequestPostProcessors.user() for security tests, already
documented from FULLST-019).
