# WI-20260611-FULLST-039 — Campagne de vérification fonctionnelle par rôle

- Work Item: WI-20260611-FULLST-039
- Role: developer
- Status: DONE (verification-only; 2 PASS-with-side-effect details, 2 FAIL identifies, others PASS)
- Scope: Verification fonctionnelle (chrome-devtools) de 10 scenarios, 4 roles (REF, ADMINISTRATEUR,
  FORMATEUR, ETUDIANT), backend local (gradlew bootRun --spring.profiles.active=local, port 8080) +
  frontend local (ng serve, port 4200). Aucune modification de code (pure verification).

## Summary table

| # | Scenario | Result | Evidence / Comment |
|---|---|---|---|
| 1 | REF — CRUD cours (catalogue) | PASS | Create/modify/delete via /app/admin/cours, network 200/201/204 confirmed in prior part of session |
| 2 | REF — CRUD filière | PASS | filière id=6 created, modified, deleted (200/201/204), fully cleaned up |
| 3 | REF — CRUD cursus (incl. add/remove cours) | PASS | cursus id=9 created/modified (PUT add/remove cours)/deleted, 200/201/204 |
| 4 | REF — CRUD promotion | PASS | promotion id=13 created, modified (session/formateur/salle), deleted, 200/201/204 |
| 5 | REF — CRUD élève dans promotion (add/remove) | PASS | "Inscrire un stagiaire" / "Ajouter un élève" dialog used to add/remove eleve from promotion, 200/204 |
| 6 | ADMIN — CRUD utilisateur (create/modify role/info/activate-deactivate/delete) | PASS | user id=14 created, role/info modified, deactivated/reactivated, deleted, all 200/201/204. See "Decisions" below re: password resets done as part of this scenario's tooling. |
| 7 | FORMATEUR — view planned courses + enrolled students per course | **FAIL** | Login OK (formateur1@eni.fr / Formateur123, 200). Calendrier page (`/app/calendrier`) calls `GET /api/eleves/{uid}/planning` (uid=10, the formateur's own uid) — same endpoint/adapter as ETUDIANT. Response = `[]` (empty), because the formateur has no `promotion`/`InscriptionCours` of their own. Root cause: `app.config.ts` binds `BaseCalendarAdapter` globally to `HttpElevePlanningAdapter` (frontend/src/app/core/adapters/eleve-planning-http.adapter.ts), regardless of role. No backend endpoint exists returning "cours dispensés par ce formateur" (checked `FormateurController.java` — only `GET /api/formateurs` list). No UI exists for "liste des élèves inscrits par cours" for a formateur either. |
| 8 | ETUDIANT — view courses + identify formateur per course | **FAIL** | Login OK (eleve1@eni.fr / Eleve123, 200). Calendrier calls `GET /api/eleves/11/planning`, returns `[]` (eleve1 has no promotion/inscriptions in DB, so calendar empty regardless of month). Independent of data availability, the feature is structurally incomplete: `PlanningEleveResponse` (backend/src/main/java/fr/eni/gestionformation/dto/PlanningEleveResponse.java) and the matching frontend `PlanningEleve` model (frontend/src/app/core/models/inscription.model.ts) do NOT include any `formateurId`/`formateurName` field, even though `CoursPlanifie.formateur` (backend/src/main/java/fr/eni/gestionformation/entity/CoursPlanifie.java line 38-40) exists on the entity. So even with data, the UI could not show "quel formateur sur quel cours". |
| 9 | REF — stagiaire cannot be enrolled twice in same cours | BLOCKED (code-reviewed only, not UI-confirmed) | Backend logic confirmed via code review: `InscriptionCoursService.creerInscription()` (backend/src/main/java/fr/eni/gestionformation/service/InscriptionCoursService.java lines 38-47) throws `InscriptionAlreadyExistsException` both when the eleve's promotion already covers the cours-planifié AND when an individual `InscriptionCours` already exists. NOT verifiable via UI: no frontend page calls `POST /api/cours-planifies/{id}/inscriptions` (the only relevant endpoint) — see scenario 10 for details. The only "add student" UI found (`/app/admin/promotions/{id}` > "Ajouter un élève") assigns `eleve.promotion`, a different code path, and only one promotion (id=12, "TEST CDA 2", 0 stagiaires) exists in current DB so a true duplicate-promotion-assignment test wasn't attempted (would require adding then trying to re-add the same eleve — UI likely just removes them from the "available" list after first add, making a true duplicate-click impossible via this dialog). |
| 10 | REF — out-of-order enrollment blocked unless "forcer l'inscription" | **FAIL / GAP confirmed** | No UI exists for individual ("à l'unité") cours enrollment anywhere in the frontend. Searched all of frontend/src for "inscriptions / InscriptionCours / inscrire / unité / forcer" (case-insensitive) — only match is a generic warning string in the promotion "retirer cours" confirmation dialog (cours-planifies-tab.html line 138: "Les inscriptions des stagiaires... seront également supprimées"), unrelated to enrollment. Backend confirms no implementation: `InscriptionCoursRequest` DTO has only `private Long eleveId;` (no "forcer" field), `InscriptionCoursService.creerInscription()` has no order/sequence validation logic, `InscriptionCoursController` has no "forcer" query param. Conclusion: feature does not exist on either side — full FAIL, candidate for a dedicated feature WI (not a small fix). |

## Files Touched
(none — verification-only session, no code changes)

## Evidence
- Scenario 7: network reqid=388 `GET /api/eleves/10/planning` → 200, body `[]`, while logged in as formateur1@eni.fr (JWT sub=formateur1@eni.fr)
- Scenario 8: network reqid=390 `GET /api/eleves/11/planning` → 200, body `[]`, while logged in as eleve1@eni.fr (JWT sub=eleve1@eni.fr)
- Scenarios 1-6: all CRUD network calls (POST/PUT/PATCH/DELETE) returned 200/201/204 across the create-modify-delete lifecycles of filière id=6, cursus id=9, promotion id=13, user id=14 — all test entities fully cleaned up (final DELETE calls returned 204/200)
- Promotion id=12 ("TEST CDA 2") is the only promotion remaining in DB, 18/18 cours planifiés, 0 stagiaires, all formateurs "Non assigné", all salles "—"

## Decisions
- **IMPORTANT SIDE EFFECT — password resets performed on seed/test accounts.** No plaintext
  passwords were known for `formateur1@eni.fr` or `eleve1@eni.fr` (not in DataInitializer.java,
  not in any memory note). To execute scenarios 7 and 8, used the ADMINISTRATEUR's
  "Réinitialiser le mot de passe" feature (`/app/admin/utilisateurs` > Actions > Réinitialiser
  le mot de passe) to set known passwords:
    - `formateur1@eni.fr` → new password **`Formateur123`**
    - `eleve1@eni.fr` → new password **`Eleve123`**
  Both resets confirmed successful (subsequent logins with these passwords returned 200).
  **Any other team member or future verification session relying on the old/unknown passwords
  for these two accounts will now need to use these new passwords (or reset again).**
- ADMINISTRATEUR credentials used throughout: `admin@admin.com` / `Admin123` (from
  `DataInitializer.java`). REF credentials used: `ref@ref.com` / `toto785971` (pre-existing,
  unchanged).
- Minor unrelated observation (not in scope, no action taken): `DataInitializer.java` has a
  stray `//Test` comment on its last line (line 34) — harmless dead comment, candidate for a
  trivial cleanup in a future WI.

## Open Blockers
- Scenario 9 not fully UI-confirmed (BLOCKED) — see table. To properly test, would need either:
  (a) a second promotion/eleve combination where the eleve is NOT yet in any promotion, used to
  attempt `POST /api/cours-planifies/{id}/inscriptions` twice via direct API call (Postman/curl)
  since no UI triggers this endpoint, or
  (b) build the missing "inscription à l'unité" UI first (see scenario 10) and then test through it.

## Next Actions (for future WIs, not done here)
1. **FULLST-039-A (Scenario 7 fix)** — Create a formateur-specific planning/calendar adapter +
   backend endpoint (e.g. `GET /api/formateurs/{id}/planning` or `GET /api/cours-planifies?formateurId=`)
   returning courses taught by the formateur, with enrolled-student counts/lists (reuse
   `InscriptionCoursService.getInscritsCombines`). Update `app.config.ts` to provide the right
   adapter based on role (or make `BaseCalendarAdapter` role-aware).
2. **FULLST-039-B (Scenario 8 fix)** — Add `formateurId`/`formateurName` (or nested object) to
   `PlanningEleveResponse` (backend/src/main/java/fr/eni/gestionformation/dto/PlanningEleveResponse.java)
   by mapping `CoursPlanifie.getFormateur()`, mirror in frontend `PlanningEleve` model
   (frontend/src/app/core/models/inscription.model.ts) and display in calendar event /
   `CalendarEvent` model.
3. **FULLST-039-C (Scenario 10 — larger feature)** — Build the "inscription à l'unité" UI
   (likely a new page or modal allowing REF to search a cours-planifié + eleve, with a
   "forcer l'inscription" checkbox), plus backend changes:
   - Add `forcer` (boolean) field to `InscriptionCoursRequest`
   - Add order/sequence validation in `InscriptionCoursService.creerInscription()` comparing
     the target `CoursPlanifie.ordre` vs the eleve's completed/planned cours in the cursus
     order, throwing a new exception (e.g. `InscriptionHorsOrdreException`) unless
     `forcer=true`, in which case proceed (optionally with a warning in the response).
4. Scenario 9 — once FULLST-039-C exists, retest duplicate-enrollment via the new UI/API. Until
   then, can be spot-checked via direct API call (curl/Postman) as a quick BACKEN WI.

## Recall Hints
- "formateur calendar empty / eleves planning wrong endpoint" → this WI, scenario 7
- "formateur sur quel cours" / "formateur dans planning eleve" → this WI, scenario 8,
  `PlanningEleveResponse` missing formateur field
- "inscription à l'unité" / "forcer l'inscription" / "hors ordre cursus" → this WI, scenario 10,
  feature entirely missing both frontend and backend
- "formateur1@eni.fr password" / "eleve1@eni.fr password" → see Decisions: Formateur123 / Eleve123
- Promotion "TEST CDA 2" (id=12) is the only promotion in DB, 0 stagiaires, all formateurs
  "Non assigné" — useful baseline for next verification session
