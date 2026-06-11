# WI-20260611-FULLST-011 — Backend: CoursPlanifie.formateur + salle, endpoint, conflits

## Work Item
WI-20260611-FULLST-011

## Role
developer

## Status
READY_FOR_REVIEW

## Scope
Add nullable `formateur` (ManyToOne User, role FORMATEUR) and `salle` (String) fields to `CoursPlanifie`.
Extend the existing `PUT /api/promotions/{id}/planning/{coursPlanifieId}` endpoint (PlanningUpdateRequest) to set/update these fields, validate the formateur role, detect formateur scheduling conflicts on the new direct field (non-blocking warnings, all promotions combined), and expose the new fields in `CoursPlanifieResponse`.

## Files Touched
- `backend/src/main/java/fr/eni/gestionformation/entity/CoursPlanifie.java` — added `formateur` (ManyToOne User, `formateur_id`, nullable) and `salle` (String, nullable); excluded `formateur` from `@ToString`/`@EqualsAndHashCode`.
- `backend/src/main/java/fr/eni/gestionformation/dto/PlanningUpdateRequest.java` — added `formateurId` (Long, nullable) and `salle` (String, nullable).
- `backend/src/main/java/fr/eni/gestionformation/dto/CoursPlanifieResponse.java` — added `formateurId` (Long), `formateurNom` (String, "Prenom Nom" or null), `salle` (String).
- `backend/src/main/java/fr/eni/gestionformation/repository/CoursPlanifieRepository.java` — added `findOverlappingForFormateurAssigne(Long formateurId, LocalDate dateDebut, LocalDate dateFin)` querying on `pc.formateur.uid` (distinct from the existing `findOverlappingForFormateur` which queries `pc.cours.formateurs`).
- `backend/src/main/java/fr/eni/gestionformation/service/PromotionService.java` — `updatePlanning(...)`:
  - sets `coursPlanifie.salle` from request (nullable, overwrite).
  - if `request.formateurId` provided: loads User, throws `UserNotFoundException` if missing, throws `IllegalArgumentException` if `role != FORMATEUR` (same pattern as `CoursService.assignFormateurs`); else sets `formateur=null`.
  - new conflict block: if `coursPlanifie.formateur != null`, calls `findOverlappingForFormateurAssigne`, excludes self by id, adds warning `"Conflit formateur : <Prenom> <Nom> déjà occupé du <debut> au <fin>"` + (`" sur la promotion <name>"` if the conflicting session has a promotion, else `" sur une autre session"`). This is in addition to (not replacing) the existing cours-level formateur conflict check (BACKEN-018).
- `backend/src/main/java/fr/eni/gestionformation/controller/PromotionController.java` — `toCoursPlanifieResponse(...)` now also maps `formateurId`, `formateurNom`, `salle`.
- `backend/src/test/java/fr/eni/gestionformation/service/PromotionServiceTest.java` — added 3 tests:
  - `updatePlanning_assigneFormateurEtSalle_metAJourCoursPlanifie`
  - `updatePlanning_formateurAssigneNonFormateur_lanceIllegalArgument`
  - `updatePlanning_conflitFormateurAssigneSurAutreSession_ajouteWarningConflit`
  - Fixed import (added `Role`).

## Frontend-facing contract (for FULLST-012)

### Request — `PUT /api/promotions/{promotionId}/planning/{coursPlanifieId}`
```json
{
  "dateDebut": "2026-06-15",
  "dateFin": "2026-06-17",
  "formateurId": 7,      // optional, Long; null/absent clears the formateur assignment
  "salle": "Salle 12"    // optional, String; null/absent clears salle
}
```
Note: `salle` and `formateurId` are NOT partial-update — sending the request without them (or with `null`) clears the existing value (overwrite semantics, same as `dateDebut`/`dateFin`).

### Response — `CoursPlanifieResponse` (used in `PromotionResponse.planning[]` and as the direct return of the PUT endpoint)
```json
{
  "id": 100,
  "coursId": 1,
  "coursNom": "Cours A",
  "dateDebut": "2026-06-15",
  "dateFin": "2026-06-17",
  "ordre": 0,
  "statut": "PLANIFIE",
  "formateurId": 7,           // null if no formateur assigned
  "formateurNom": "Marie Curie", // null if no formateur assigned
  "salle": "Salle 12",        // null if not set
  "warnings": []
}
```
- `formateurNom` format: `"<firstName> <lastName>"`.
- `warnings` is always `[]` on `GET` (PIT-008 still applies — unchanged), populated only on the PUT response.
- Validation errors: `formateurId` not a User -> `UserNotFoundException` (404, see GlobalExceptionHandler); `formateurId` references a User with `role != FORMATEUR` -> `IllegalArgumentException` (mapped per CONV-003, check GlobalExceptionHandler for the exact HTTP status — same as `CoursService.assignFormateurs`).

## Evidence
- `cd backend; ./gradlew test` -> exit code 0, no failures (BUILD SUCCESSFUL, quiet output).
- `./gradlew test --tests "*PromotionServiceTest*"` -> compiled and ran clean after fixing `Role.ELEVE` -> `Role.ETUDIANT` typo in new test code (Role enum values: `ETUDIANT, REFERENTE_ADMINISTRATIVE, ADMINISTRATEUR, FORMATEUR`).

## Decisions
- Kept the existing cours-level formateur conflict check (`findOverlappingForFormateur` via `cours.getFormateurs()`, BACKEN-018) untouched and added the new session-level check (`findOverlappingForFormateurAssigne` via `coursPlanifie.formateur`) as an additional, separate warning block — both can fire independently and are non-blocking (consistent with PIT-008: warnings only returned on PUT).
- `salle`/`formateurId` use full-overwrite semantics on PUT, matching the existing `dateDebut`/`dateFin` behavior in `PlanningUpdateRequest` (no PATCH/partial-update pattern exists in this codebase).
- Did not create a dedicated `PUT /api/cours-planifies/{id}` endpoint — extended the existing `PlanningUpdateRequest`/`updatePlanning` per the "or extension" option in the WI notes, since the endpoint already operates on a single `CoursPlanifie` by id and adding fields is the smaller, more consistent change.
- `ddl-auto=update` will add `formateur_id` (FK to `users`) and `salle` columns to `cours_planifie` automatically in local dev — no manual migration needed (per WI instructions).

## Open Blockers
None.

## Next Actions
- FULLST-012 (frontend) can consume `formateurId`/`formateurNom`/`salle` in `CoursPlanifieResponse` and send `formateurId`/`salle` in `PlanningUpdateRequest` PUT body.
- Manager/reviewer: confirm HTTP status mapping for `IllegalArgumentException` (formateur role validation) is acceptable per CONV-003 — reused existing pattern from `CoursService.assignFormateurs`, no new exception type introduced.

## Recall Hints
- `CoursPlanifie.formateur` (entity field) vs `Cours.formateurs` (existing list) — two different concepts, do not confuse. The new conflict check (`findOverlappingForFormateurAssigne`) is on `CoursPlanifie.formateur`.
- Repository method names: `findOverlappingForFormateur` (existing, cours-based) vs `findOverlappingForFormateurAssigne` (new, session-based).

## Proposed Rules
- TYPE: CONVENTION
  Title: PlanningUpdateRequest fields use full-overwrite (not partial-update) semantics
  Scope: backend/src/main/java/fr/eni/gestionformation/dto/PlanningUpdateRequest.java, PromotionService.updatePlanning
  Rule: Every field added to PlanningUpdateRequest (dateDebut, dateFin, formateurId, salle) is set unconditionally from the request body on each PUT — sending null/absent clears the existing value.
  Why: Keeps the update semantics consistent and predictable; avoids divergent partial-update logic per field that would be easy to get wrong.
  How to apply: When adding a new editable field to CoursPlanifie via this endpoint, follow the same `coursPlanifie.setX(request.getX())` overwrite pattern (with validation for FK-like fields such as formateurId) rather than introducing null-check "only update if present" logic.
  Evidence: backend/src/main/java/fr/eni/gestionformation/service/PromotionService.java (updatePlanning), this WI.
