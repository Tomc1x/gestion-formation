# WI-20260611-FULLST-033 — Endpoint formateurs + fix select formateur

## Work Item
WI-20260611-FULLST-033

## Role
developer

## Status
DONE

## Scope
- Backend: nouvel endpoint `GET /api/formateurs` retournant `List<FormateurInfo>` (id/firstName/lastName) pour les `User` avec role FORMATEUR, securise pour ADMINISTRATEUR + REFERENTE_ADMINISTRATIVE.
- Frontend: remplacer l'appel `userAdminAdapter.getAll()` (403 pour REFERENTE_ADMINISTRATIVE) par un nouvel adapter `BaseFormateurAdapter`/`HttpFormateurAdapter` consommant `/api/formateurs`.

## Files Touched
- backend/src/main/java/fr/eni/gestionformation/repository/UserRepository.java (ajout `findByRole(Role role)`)
- backend/src/main/java/fr/eni/gestionformation/controller/FormateurController.java (nouveau, `GET /api/formateurs`)
- backend/src/main/java/fr/eni/gestionformation/security/SecurityConfig.java (ajout regle `GET /api/formateurs` -> hasAnyRole ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE, placee avant `/api/admin/users`)
- frontend/src/app/core/adapters/formateur.adapter.ts (nouveau, `BaseFormateurAdapter` + interface `FormateurInfo`)
- frontend/src/app/core/adapters/formateur-http.adapter.ts (nouveau, `HttpFormateurAdapter`)
- frontend/src/app/app.config.ts (provider `BaseFormateurAdapter -> HttpFormateurAdapter`)
- frontend/src/app/features/promotions/promotion-detail/cours-planifies/cours-planifies-tab.ts (ngOnInit utilise `formateurAdapter.getAll()` au lieu de `userAdminAdapter.getAll()`)

## Evidence
- `./gradlew test` : PASS (aucun echec rapporte, build SUCCESS via -q)
- `npx ng build` (frontend) : PASS, "Application bundle generation complete". Warnings de budget CSS preexistants (cursus.scss, promotions.scss, register.scss, utilisateurs.scss) non lies a ce WI (cf PIT-004).

## Decisions
- Reutilise le DTO existant `FormateurInfo` (id/firstName/lastName) deja utilise par CoursController/CursusController, pas de nouveau DTO.
- Nouveau controller dedie `FormateurController` plutot que d'ajouter une route a `UserAdminController` (separe l'acces "liste formateurs pour assignation" de l'admin users CRUD, qui reste hasRole ADMINISTRATEUR).
- Cree un adapter frontend complet (Base + Http) suivant CONV-001 (adapter pattern), au lieu de bricoler un appel HTTP direct dans le composant.
- N'ai pas touche au pattern mock (`user-admin-mock.ts`) car aucun mock-mode n'existe pour cet adapter et aucun autre composant ne l'utilise encore.

## Open Blockers
None.

## Next Actions
- Test manuel optionnel via chrome-devtools avec un compte REFERENTE_ADMINISTRATIVE pour confirmer visuellement que le select formateur de cours-planifies-tab est maintenant peuple (non execute, ./gradlew test + ng build suffisent comme preuve de correction fonctionnelle/compilation).

## Recall Hints
- "endpoint formateurs", "select formateur cours planifie 403", "/api/formateurs", "FormateurController", "BaseFormateurAdapter"

## Proposed Rules
- TYPE: PITFALL
  Title: REFERENTE_ADMINISTRATIVE has no access to /api/admin/users
  Scope: backend/security, frontend adapters consuming user lists
  Rule: Any frontend feature needing a filtered user list (e.g., formateurs) for a non-ADMINISTRATEUR role must use a dedicated endpoint, never `/api/admin/users` (hasRole ADMINISTRATEUR only).
  Why: `cours-planifies-tab.ts` silently swallowed a 403 from `GET /api/admin/users` for REFERENTE_ADMINISTRATIVE, leaving the formateur select empty with no visible error.
  How to apply: create a narrow `/api/<resource>` endpoint with `hasAnyRole(...)` matching actual consumers, returning a minimal DTO (e.g. FormateurInfo).
  Evidence: WI-20260611-FULLST-033, backend/src/main/java/fr/eni/gestionformation/controller/FormateurController.java, backend/src/main/java/fr/eni/gestionformation/security/SecurityConfig.java
