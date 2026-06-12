# WI-20260611-FULLST-042 — Developer Memory Note

## Work Item
WI-20260611-FULLST-042 — Inscription a l'unite (cours individuel) + option "forcer" hors-ordre cursus

## Role
developer

## Status
DONE

## Scope
Implement the 12-step plan from `ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-042.md`:
- Backend (7 steps): allow enrolling a student individually into a `CoursPlanifie`, with optional cursus-order
  validation via `forcer` flag. Order reference = `CursusCours.ordre` (pedagogical cursus order), not
  `CoursPlanifie.ordre` (promotion planning order).
- Frontend (5 steps): "Inscrire a l'unite" modal in `cours-planifies-tab` with eleve dropdown (reusing
  `GET /api/eleves`), `forcer` checkbox, warning display, 409 error display.
- PO-confirmed hypothesis applied: `eleve.getPromotion() == null` => no order validation performed at all
  (individual enrollment always allowed regardless of `forcer`).
- Existing duplicate-enrollment checks in `InscriptionCoursService.creerInscription()` left untouched (scenario 9
  non-regression).

## Files Touched

### Backend
- `backend/src/main/java/fr/eni/gestionformation/dto/InscriptionCoursRequest.java` — added `forcer: boolean` field
- `backend/src/main/java/fr/eni/gestionformation/dto/InscriptionCoursResponse.java` — added `warnings: List<String>` field
- `backend/src/main/java/fr/eni/gestionformation/exception/InscriptionHorsOrdreException.java` — new exception (409)
- `backend/src/main/java/fr/eni/gestionformation/exception/GlobalExceptionHandler.java` — added handler mapping
  `InscriptionHorsOrdreException` to 409 CONFLICT
- `backend/src/main/java/fr/eni/gestionformation/service/InscriptionCoursService.java` — major edit:
  - new field `cursusCoursRepository`
  - new nested record `InscriptionResult(InscriptionCours inscription, List<String> warnings)`
  - `creerInscription(coursPlanifieId, eleveId, forcer)` signature change, returns `InscriptionResult`
  - new private method `calculerPrerequisManquants(User eleve, CoursPlanifie coursPlanifieCible)` computing
    missing prerequisite courses based on `CursusCours.ordre` and `getPlanningEleve()`
  - existing duplicate checks (`InscriptionAlreadyExistsException` for promotion-coverage and direct duplicate)
    left unchanged and run first
- `backend/src/main/java/fr/eni/gestionformation/controller/InscriptionCoursController.java` — updated
  `creerInscription` endpoint to pass `forcer` and map `InscriptionResult` to `InscriptionCoursResponse`

### Backend tests
- `backend/src/test/java/fr/eni/gestionformation/controller/InscriptionCoursControllerTest.java` — updated mocks
  to new `creerInscription(id, eleveId, forcer)` signature returning `InscriptionResult`
- `backend/src/test/java/fr/eni/gestionformation/service/InscriptionCoursServiceTest.java`:
  - updated 3 existing tests to new signature/return type
  - added 3 new tests:
    - `creerInscription_horsOrdreSansForcer_lanceException`
    - `creerInscription_horsOrdreAvecForcer_succesAvecWarnings`
    - `creerInscription_enOrdre_succesSansWarnings`
  - added mock `CursusCoursRepository`

### Frontend
- `frontend/src/app/core/models/inscription.model.ts` — added `CreerInscriptionRequest`, `InscriptionCours`
- `frontend/src/app/core/adapters/inscription.adapter.ts` — added abstract `creerInscription()`
- `frontend/src/app/core/adapters/inscription-http.adapter.ts` — implemented `creerInscription()` (POST
  `/api/cours-planifies/{id}/inscriptions`)
- `frontend/src/app/features/promotions/promotion-detail/cours-planifies/cours-planifies-tab.ts` — new
  "Inscription a l'unite" block: signals (`inscriptionTarget`, `eleves`, `inscriptionError`,
  `inscriptionWarnings`, `inscriptionSubmitting`), `inscriptionForm` (FormGroup with `eleveId`, `forcer`),
  `openInscriptionModal`, `closeInscriptionModal`, `submitInscription`
- `frontend/src/app/features/promotions/promotion-detail/cours-planifies/cours-planifies-tab.html` — new
  "Inscrire a l'unite" action button (LucideUserPlus icon) per planning row, and new modal dialog with eleve
  select, forcer checkbox, warnings list, 409 error display

## Evidence

### Backend tests
```
cd C:\Users\user\IdeaProjects\gestion-formation\backend
.\gradlew.bat test
```
Result: `BUILD SUCCESSFUL`. All tests pass including the 3 new `InscriptionCoursServiceTest` cases covering
hors-ordre/sans-forcer (exception), hors-ordre/avec-forcer (success + warnings), en-ordre (success, no warnings),
plus the updated controller test and the 2 pre-existing duplicate-check tests (non-regression).

### Frontend build
```
cd C:\Users\user\IdeaProjects\gestion-formation\frontend
ng build --configuration production
```
Result: build succeeds. Only pre-existing unrelated SCSS budget warnings for other features (not introduced by
this WI).

### chrome-devtools live verification
- Logged in as REFERENTE_ADMINISTRATIVE (ref@ref.com), opened promotion 12 "TEST CDA 2".
- Modal "Inscrire a l'unite" opens correctly; eleve dropdown populated from `GET /api/eleves`.
- Scenario 9 non-regression (duplicate enrollment, eleve already covered by promotion): API returned 409 with
  the EXACT pre-existing message `"L'élève 11 est déjà inscrit (directement ou via sa promotion) au cours
  planifié 31"` — confirmed unchanged.
- Happy-path (eleve without promotion, individual enrollment, no order validation per confirmed hypothesis):
  returned 201 with new `warnings: []` field present in response body — confirmed working after backend restart.

### Backend dev server restart (needed mid-session)
The running dev `bootRun` process (PID 33992, port 8080) was serving stale bytecode without the `warnings`
field. Killed via `Stop-Process -Id 33992 -Force` and restarted with
`cd backend && (.\gradlew.bat bootRun --args='--spring.profiles.active=local' > /tmp/backend.log 2>&1 &)`.
Server is currently running in background on port 8080 (verified via `curl http://localhost:8080/api/eleves`
returning 403, i.e. up and requiring auth as expected).

## Decisions
- PO-confirmed hypothesis implemented literally: if `eleve.getPromotion() == null` OR
  `eleve.getPromotion().getCursus() == null`, `calculerPrerequisManquants` is never called and no warnings/
  exceptions related to ordering are produced — individual enrollment proceeds unconditionally (subject only to
  the pre-existing duplicate checks).
- Order reference strictly uses `CursusCours.ordre` via `cursusCoursRepository.findByCursusIdOrderByOrdre(cursusId)`,
  matched against the target `CoursPlanifie.getCours().getId()`. Prerequisites = all `CursusCours` entries with
  `ordre < ordreCible`. A prerequisite is "covered" if `getPlanningEleve(eleveId)` contains any `CoursPlanifie`
  whose `getCours().getId()` matches the prerequisite's cours id (regardless of statut/origine) — reuses the
  existing combined promotion+individual planning view, deduplicated by construction.
- `forcer=false` and missing prerequisites => `InscriptionHorsOrdreException` (409, dedicated message listing
  missing course names, mapped via `GlobalExceptionHandler`).
- `forcer=true` and missing prerequisites => enrollment proceeds, with one warning string appended per the
  architect's pattern (matches existing `warnings: List<String>` convention used elsewhere, e.g.
  `CoursPlanifieResponse.warnings`, `PromotionService.updatePlanning`).
- Existing duplicate checks (`dejaCouvertParPromotion` and `existsByEleveUidAndCoursPlanifieId`) run BEFORE the
  hors-ordre logic and were not modified — preserves scenario 9 behavior exactly.

## Open Blockers
None. Implementation complete, builds pass, unit tests pass, core scenarios verified live.

One item NOT verified end-to-end via chrome-devtools/live API (documented as accepted gap, not a blocker):
- The hors-ordre 409 (`InscriptionHorsOrdreException`) and hors-ordre+forcer (201 + warnings) response paths were
  not exercised through the live UI/API, because no existing eleve in the seed data has a cursus reference with
  an enrollment gap in an early-ordre course suitable for triggering this path without corrupting other test
  data. An attempt to set up such a scenario (giving "Eleve Deux", uid=12, a promotion with cursus) was
  abandoned because it caused `getPlanningEleve` to report ALL 18 promotion-12 `CoursPlanifie` via PROMOTION
  origin, making every cursus prerequisite trivially "covered" — hors-ordre could never trigger via this route.
  This path IS fully covered by the 3 new unit tests (`creerInscription_horsOrdreSansForcer_lanceException`,
  `creerInscription_horsOrdreAvecForcer_succesAvecWarnings`, `creerInscription_enOrdre_succesSansWarnings`), which
  mock the repositories directly and exercise `calculerPrerequisManquants` and the branching logic precisely.

## DB side-effects during testing (created and fully reverted)
1. `UPDATE users SET promotion_id=12 WHERE uid=12;` (Eleve Deux) — done to attempt hors-ordre test setup.
   Reverted: `UPDATE users SET promotion_id=NULL WHERE uid=12;` (UPDATE 1).
2. A 201 happy-path test created `inscription_cours` row `id=4` (user_id=12, cours_planifie_id=31,
   date_inscription=2026-06-11). Reverted: `DELETE FROM inscription_cours WHERE user_id=12 AND
   cours_planifie_id=31;` (DELETE 1).
3. Verified final state matches pre-test snapshot: `SELECT uid, promotion_id FROM users WHERE uid IN (11,12);`
   => `11 | 12`, `12 | NULL`. Promotion 12 page reload shows STAGIAIRES=1, COURS PLANIFIÉS=18/18, every cours row
   INSCRITS=1 — identical to the original snapshot taken before any testing began.

## Next Actions
None required for this WI. If the manager wants the hors-ordre 409/forcer UI paths verified live, a dedicated
test-data setup (a standalone eleve with `promotion.cursus` set but NOT enrolled via that promotion's planning,
e.g. a promotion whose `CoursPlanifie` list is empty/different from the cursus) would be needed — out of scope
for this WI's verification given existing seed data.

## Recall Hints
- `InscriptionCoursService.calculerPrerequisManquants` — core hors-ordre logic, keyed off `CursusCours.ordre`
- `InscriptionCoursService.InscriptionResult` — new record wrapping `(InscriptionCours, List<String> warnings)`
- `InscriptionHorsOrdreException` — new 409 exception, handled in `GlobalExceptionHandler`
- Frontend modal: `cours-planifies-tab.ts` / `.html`, section "Inscription a l'unite", `inscriptionForm`
- Backend dev server was restarted mid-session (bootRun, port 8080) — currently running in background with the
  new code.

## Proposed Rules
- TYPE: PITFALL
  Title: Seed eleve with promotion+cursus makes hors-ordre validation untestable via UI
  Scope: backend/inscription, any future WI touching cursus-order validation or `getPlanningEleve`
  Rule: Do not set `users.promotion_id` on a seed eleve to a promotion whose planning already covers the
  target cursus's early courses when trying to manually test hors-ordre/prerequisite logic — `getPlanningEleve`
  will report those courses as covered via PROMOTION origin, masking any gap.
  Why: Discovered during WI-042 verification — attempted setup made all 18 promotion-12 courses appear
  "inscrit" for the test eleve, so `calculerPrerequisManquants` always returned an empty list regardless of
  the target course's ordre.
  How to apply: To manually test hors-ordre scenarios, use an eleve whose `promotion.cursus` is set but whose
  `promotion.planning` does NOT cover the cursus's early-ordre courses (e.g. a promotion with a partial/short
  planning), or rely on the mocked unit tests in `InscriptionCoursServiceTest` instead.
  Evidence: WI-20260611-FULLST-042, this memory note (DB side-effects section)
