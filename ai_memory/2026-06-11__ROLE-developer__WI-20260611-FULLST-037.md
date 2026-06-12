# WI-20260611-FULLST-037 — Endpoint eleves + fix liste "Ajouter un eleve"

## Work Item
WI-20260611-FULLST-037

## Role
developer

## Status
DONE

## Scope
Backend: nouvel endpoint `GET /api/eleves` retournant les utilisateurs avec role `ETUDIANT`
(id, firstName, lastName, email), securise pour `ADMINISTRATEUR` et `REFERENTE_ADMINISTRATIVE`.
Frontend: nouvel adapter `BaseEleveAdapter` / `HttpEleveAdapter`, enregistre dans `app.config.ts`,
et bascule de `stagiaires-tab.ts` (`openAddEleveModal`) depuis `userAdminAdapter.getAll()`
(`GET /api/admin/users`, reserve ADMINISTRATEUR) vers ce nouvel adapter. Meme pattern que
FULLST-033 (formateurs).

## Files Touched
- `backend/src/main/java/fr/eni/gestionformation/controller/EleveController.java` (NEW) —
  `@RestController` `/api/eleves`, `GET` -> liste `EleveDisponibleInfo` via
  `userRepository.findByRole(Role.ETUDIANT)`.
- `backend/src/main/java/fr/eni/gestionformation/dto/EleveDisponibleInfo.java` (NEW) — DTO
  `id`, `firstName`, `lastName`, `email`. ATTENTION : un DTO `EleveInfo` existait deja
  (utilise par `PromotionController.toEleveInfo`, 3 champs sans email, pour la liste des
  eleves d'une promotion). Pour eviter toute collision/regression j'ai cree un DTO distinct
  `EleveDisponibleInfo` au lieu de modifier `EleveInfo` existant.
- `backend/src/main/java/fr/eni/gestionformation/security/SecurityConfig.java` — ajout
  `.requestMatchers(HttpMethod.GET, "/api/eleves").hasAnyRole("ADMINISTRATEUR", "REFERENTE_ADMINISTRATIVE")`
  juste apres la regle existante `GET /api/eleves/*/planning` (qui reste prioritaire car
  Spring Security evalue les matchers dans l'ordre et `/api/eleves/*/planning` est plus
  specifique et place avant).
- `frontend/src/app/core/adapters/eleve.adapter.ts` (NEW) — `BaseEleveAdapter` abstrait +
  interface `EleveInfo` (id, firstName, lastName, email).
- `frontend/src/app/core/adapters/eleve-http.adapter.ts` (NEW) — `HttpEleveAdapter`,
  `GET /api/eleves`.
- `frontend/src/app/app.config.ts` — import + provider
  `{provide: BaseEleveAdapter, useClass: HttpEleveAdapter}`.
- `frontend/src/app/features/promotions/promotion-detail/stagiaires/stagiaires-tab.ts` —
  remplace `BaseUserAdminAdapter` par `BaseEleveAdapter` ; `openAddEleveModal()` appelle
  `eleveAdapter.getAll()` (pas de filtre `role === 'ETUDIANT'` necessaire, le backend ne
  retourne deja que les ETUDIANT) et filtre uniquement sur `!currentIds.has(u.id)`.

## Evidence
- `cd backend && ./gradlew test` -> BUILD SUCCESSFUL (toutes suites passent, y compris
  apres ajout de `EleveController`/`EleveDisponibleInfo`/regle SecurityConfig).
- `cd frontend && npx ng build` -> succes, warnings preexistants uniquement (budgets SCSS
  utilisateurs/promotions/cursus/register, non lies a ce WI).
- Verification end-to-end via chrome-devtools (voir aussi note FULLST-038) :
  - Login `ref@ref.com` / `toto785971` (role REFERENTE_ADMINISTRATIVE).
  - Promotion `TEST CDA 2` (id=12), onglet Stagiaires, clic "Ajouter un élève".
  - Requete reseau `GET /api/eleves` -> 200 (avant le fix backend redemarre proprement :
    403 car l'ancien process bootRun ne contenait pas le build avec la nouvelle regle
    SecurityConfig — voir Decisions).
  - Modale affiche "Eleve Un / eleve1@eni.fr", "Eleve Deux / eleve2@eni.fr",
    "Eleve eleve / eleve@eleve.com" (les 3 ETUDIANT du seed).
  - Clic "Ajouter Eleve Un" -> compteur Stagiaires passe de 0 a 1, ligne ajoutee au tableau,
    "Eleve Un" disparait de la liste disponible (filtre `currentIds`).
  - Clic "Retirer Eleve Un" -> retour a 0 stagiaires (nettoyage de l'etat de test).

## Decisions
- Cree `EleveDisponibleInfo` plutot que de reutiliser/modifier `EleveInfo` existant (utilise
  par `PromotionController` pour les eleves d'une promotion, 3 champs sans email). Ajouter
  `email` au `EleveInfo` existant aurait change un contrat deja utilise ailleurs ; un DTO
  separe est conforme au pattern "un DTO leger par cas d'usage" deja vu (`FormateurInfo`
  vs autres DTO User).
- Nouveau controller dedie `EleveController` (`/api/eleves`, `GET`) plutot que d'ajouter
  une methode dans `InscriptionCoursController` (qui possede deja `/api/eleves/{id}/planning`) :
  garde la separation par ressource/responsabilite, meme decoupage que `FormateurController`.
- Ordre des matchers SecurityConfig : `GET /api/eleves/*/planning` (authenticated, plus
  specifique) doit rester AVANT `GET /api/eleves` (role REF/ADMIN) sinon le matcher generique
  capturerait aussi `/api/eleves/{id}/planning` et bloquerait un ETUDIANT consultant son
  propre planning. Verifie : ordre conserve, les deux regles cohabitent sans conflit
  (le matcher avec wildcard `/*/planning` est evalue avant `/api/eleves` exact).

## Open Blockers
Aucun.

## Next Actions
- FULLST-038 traite dans la meme session (voir note dediee) — le rendu de la modale avec
  une liste peuplee a ete verifie pendant cette meme passe chrome-devtools.

## Recall Hints
- Pattern "endpoint role-filtre + adapter dedie" : voir `FormateurController` /
  `formateur.adapter.ts` / `formateur-http.adapter.ts` (FULLST-033), reproduit ici pour
  les eleves.
- Comptes de test : REF `ref@ref.com` / `toto785971` ; ETUDIANT seed `eleve1@eni.fr`,
  `eleve2@eni.fr`, `eleve@eleve.com` (visibles via `/api/eleves`).
- Si un test manuel renvoie 403 sur un endpoint juste modifie dans SecurityConfig alors que
  le code compile et que les tests passent : le process `bootRun` lance avant la
  modification servait un ancien build. Il faut `./gradlew --stop` (tuer les daemons) +
  relancer `bootRun` pour que SecurityConfig recharge la nouvelle config (le rechargement
  a chaud de Spring Security filter chain n'est pas garanti avec bootRun simple).

## Proposed Rules
- TYPE: PITFALL
  Title: bootRun ne recharge pas toujours SecurityConfig apres edition
  Scope: backend/src/main/java/fr/eni/gestionformation/security/SecurityConfig.java, verifications manuelles via bootRun
  Rule: Apres une modification de SecurityConfig.java pendant une session de verification manuelle, arreter les daemons Gradle (`./gradlew --stop`) et relancer `bootRun` plutot que de compter sur un rechargement a chaud.
  Why: WI-20260611-FULLST-037 — un `GET /api/eleves` securise correctement dans le code (tests OK) renvoyait 403 via le bootRun deja en cours, lance avant la recompilation complete ; redemarrage propre a resolu le 403.
  How to apply: avant de tester manuellement un changement de securite via chrome-devtools, killer les process java existants et relancer `./gradlew bootRun --args='--spring.profiles.active=local'` depuis zero.
  Evidence: ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-037.md (section Evidence/Decisions)
